import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { vi } from 'vitest';

import { activeCompanyGuard, authGuard, driverGuard, guestGuard, officeGuard } from './auth.guard';
import { AuthService } from './auth.service';
import { authInterceptor } from '../http/auth.interceptor';

describe('AuthService', () => {
  const storageKey = 'erp.auth.session';
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    localStorage.removeItem(storageKey);

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });

    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.removeItem(storageKey);
  });

  function base64url(value: string): string {
    return btoa(value).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  }

  function makeJwt(payload: Record<string, unknown>): string {
    return `${base64url('{"alg":"none"}')}.${base64url(JSON.stringify(payload))}.sig`;
  }

  function login(auth: AuthService, overrides: Record<string, unknown> = {}): void {
    auth.login({ email: 'admin@example.com', password: 'password123' }).subscribe();

    const request = httpTesting.expectOne('/api/v1/auth/login');
    expect(request.request.method).toBe('POST');
    request.flush({
      accessToken: 'token-123',
      tokenType: 'Bearer',
      expiresIn: 3600,
      user: { id: '1', email: 'admin@example.com' },
      availableCompanies: [
        { companyId: 'company-1', roles: [{ id: 'r-admin', code: 'ADMIN', name: 'Admin' }] },
        { companyId: 'company-2', roles: [{ id: 'r-mgr', code: 'MANAGER' }] },
      ],
      ...overrides,
    });
  }

  function selectCompany(
    auth: AuthService,
    companyId = 'company-1',
    roles: { id: string; code: string; name?: string }[] = [{ id: 'r-admin', code: 'ADMIN', name: 'Admin' }],
    effectivePermissions: string[] = ['iam:role:read', 'user:write'],
  ): void {
    auth
      .selectCompany(companyId, { id: companyId, name: 'ACME', companyCode: 101 })
      .subscribe();

    const request = httpTesting.expectOne('/api/v1/auth/select-company');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ companyId });
    expect(request.request.headers.get('Authorization')).toBe('Bearer token-123');
    request.flush({
      accessToken: `scoped-${companyId}`,
      tokenType: 'Bearer',
      expiresIn: 1800,
      activeCompanyId: companyId,
      roles,
      effectivePermissions,
    });
  }

  function selectPlatform(auth: AuthService, accessToken = 'platform-token', effectivePermissions: string[] = []): void {
    auth.selectPlatform().subscribe();

    const request = httpTesting.expectOne('/api/v1/auth/select-platform');
    expect(request.request.method).toBe('POST');
    expect(request.request.headers.get('Authorization')).toBe('Bearer token-123');
    request.flush({
      accessToken,
      tokenType: 'Bearer',
      expiresIn: 1800,
      role: 'PLATFORM_ADMIN',
      effectivePermissions,
    });
  }

  it('starts without a persisted session', () => {
    const auth = TestBed.inject(AuthService);

    expect(auth.session()).toBeNull();
    expect(auth.isAuthenticated()).toBe(false);
    expect(auth.authorizationHeader()).toBeNull();
  });

  it('stores a successful login session with roles arrays and permissions', () => {
    const auth = TestBed.inject(AuthService);

    login(auth);

    expect(auth.isAuthenticated()).toBe(true);
    expect(auth.hasActiveCompany()).toBe(false);
    expect(auth.authorizationHeader()).toBeNull();
    expect(auth.globalAuthorizationHeader()).toBe('Bearer token-123');
    expect(auth.session()?.user).toEqual({ id: '1', email: 'admin@example.com' });
    expect(auth.availableCompanies()[0]?.roles).toEqual([{ id: 'r-admin', code: 'ADMIN', name: 'Admin' }]);

    const storedSession = JSON.parse(localStorage.getItem(storageKey) ?? '{}');
    expect(storedSession.globalAccessToken).toBe('token-123');
    expect(storedSession.scopedRoles).toEqual([]);
  });

  it('activates a scoped token, roles, and permissions directly from login', () => {
    const auth = TestBed.inject(AuthService);

    auth.login({ email: 'driver@example.com', password: 'password123' }).subscribe();
    httpTesting.expectOne('/api/v1/auth/login').flush({
      accessToken: 'global-token',
      tokenType: 'Bearer',
      expiresIn: 3600,
      user: { id: 'driver-user', email: 'driver@example.com' },
      availableCompanies: [
        {
          companyId: 'company-1',
          tradeName: 'ACME',
          cnpj: '00.000.000/0001-00',
          roles: [{ id: 'r-driver', code: 'DRIVER' }],
        },
      ],
      scopedToken: {
        accessToken: 'driver-scoped-token',
        tokenType: 'Bearer',
        expiresIn: 1800,
        activeCompanyId: 'company-1',
        roles: [{ id: 'r-driver', code: 'DRIVER' }],
        effectivePermissions: ['logistics:shipment:read'],
      },
    });

    expect(auth.authorizationHeader()).toBe('Bearer driver-scoped-token');
    expect(auth.globalAuthorizationHeader()).toBe('Bearer global-token');
    expect(auth.session()?.activeCompany).toEqual({ id: 'company-1', name: 'ACME' });
    expect(auth.hasRoleCode('DRIVER')).toBe(true);
    expect(auth.hasRoleCode('ADMIN')).toBe(false);
    expect(auth.hasPermission('logistics:shipment:read')).toBe(true);
    expect(auth.landingPath()).toBe('/driver/shipments');
  });

  it('stores the selected company, scoped roles, and permissions', () => {
    const auth = TestBed.inject(AuthService);

    login(auth);
    selectCompany(auth);

    expect(auth.hasActiveCompany()).toBe(true);
    expect(auth.authorizationHeader()).toBe('Bearer scoped-company-1');
    expect(auth.session()?.activeCompany).toEqual({ id: 'company-1', name: 'ACME', companyCode: 101 });
    expect(auth.roles().map((r) => r.code)).toEqual(['ADMIN']);
    expect(auth.hasPermission('user:write')).toBe(true);
    expect(auth.hasAnyPermission(['iam:role:write', 'user:write'])).toBe(true);
    expect(auth.hasAllPermissions(['iam:role:read', 'iam:role:write'])).toBe(false);
  });

  it('uses the global login token when switching companies after a scoped token is active', () => {
    const auth = TestBed.inject(AuthService);

    login(auth);
    selectCompany(auth);

    auth.selectCompany('company-2', { id: 'company-2', name: 'Beta' }).subscribe();

    const request = httpTesting.expectOne('/api/v1/auth/select-company');
    expect(request.request.headers.get('Authorization')).toBe('Bearer token-123');
    request.flush({
      accessToken: 'scoped-company-2',
      tokenType: 'Bearer',
      expiresIn: 1800,
      activeCompanyId: 'company-2',
      roles: [{ id: 'r-mgr', code: 'MANAGER' }],
      effectivePermissions: [],
    });

    expect(auth.authorizationHeader()).toBe('Bearer scoped-company-2');
    expect(auth.session()?.activeCompany).toEqual({ id: 'company-2', name: 'Beta' });
    expect(auth.hasRoleCode('MANAGER')).toBe(true);
  });

  it('selects the platform token and routes platform requests to it', () => {
    const auth = TestBed.inject(AuthService);

    login(auth);
    selectPlatform(auth, 'platform-token', ['platform:company:read', 'platform:company:write']);

    expect(auth.platformAuthorizationHeader()).toBe('Bearer platform-token');
    expect(auth.hasPermission('platform:company:write')).toBe(false);
    expect(auth.session()?.platformPermissions).toContain('platform:company:write');
    expect(auth.session()?.platformRole).toBe('PLATFORM_ADMIN');
  });

  it('attaches the right token per request group via headerForUrl', () => {
    const auth = TestBed.inject(AuthService);

    expect(auth.headerForUrl('/api/v1/auth/login')).toBeNull();

    login(auth);
    expect(auth.headerForUrl('/api/v1/auth/select-company')).toBe('Bearer token-123');

    selectPlatform(auth);
    expect(auth.headerForUrl('/api/v1/platform/companies')).toBe('Bearer platform-token');

    selectCompany(auth);
    expect(auth.headerForUrl('/api/v1/users')).toBe('Bearer scoped-company-1');
    expect(auth.headerForUrl('/api/v1/iam/roles')).toBe('Bearer scoped-company-1');
  });

  it('detects platform admin capability from the global token authorities', () => {
    const auth = TestBed.inject(AuthService);

    auth.login({ email: 'admin@example.com', password: 'password123' }).subscribe();
    httpTesting.expectOne('/api/v1/auth/login').flush({
      accessToken: makeJwt({ token_use: 'global', authorities: ['platform:company:read'] }),
      tokenType: 'Bearer',
      expiresIn: 3600,
      user: { id: '1', email: 'admin@example.com' },
      availableCompanies: [],
    });

    expect(auth.canActAsPlatformAdmin()).toBe(true);
  });

  it('restores a persisted scoped session after the app reloads', () => {
    localStorage.setItem(
      storageKey,
      JSON.stringify({
        user: { id: '1', email: 'admin@example.com' },
        globalAccessToken: 'token-123',
        globalTokenType: 'Bearer',
        globalExpiresAt: Date.now() + 3600 * 1000,
        scopedAccessToken: 'scoped-123',
        scopedTokenType: 'Bearer',
        scopedExpiresAt: Date.now() + 1800 * 1000,
        scopedRoles: [{ id: 'r-admin', code: 'ADMIN' }],
        scopedPermissions: ['user:write'],
        scopedActiveCompanyId: 'company-1',
        availableCompanies: [{ companyId: 'company-1', roles: [{ id: 'r-admin', code: 'ADMIN' }] }],
        activeCompany: { id: 'company-1', name: 'ACME', companyCode: 101 },
        platformPermissions: [],
      }),
    );

    const auth = TestBed.inject(AuthService);

    expect(auth.isAuthenticated()).toBe(true);
    expect(auth.hasActiveCompany()).toBe(true);
    expect(auth.authorizationHeader()).toBe('Bearer scoped-123');
    expect(auth.globalAuthorizationHeader()).toBe('Bearer token-123');
    expect(auth.hasRoleCode('ADMIN')).toBe(true);
  });

  it('does not restore an expired global session', () => {
    localStorage.setItem(
      storageKey,
      JSON.stringify({
        globalAccessToken: 'token-123',
        globalTokenType: 'Bearer',
        globalExpiresAt: Date.now() - 1000,
        scopedRoles: [],
        scopedPermissions: [],
        platformPermissions: [],
        availableCompanies: [],
      }),
    );

    const auth = TestBed.inject(AuthService);

    expect(auth.session()).toBeNull();
    expect(auth.isAuthenticated()).toBe(false);
    expect(localStorage.getItem(storageKey)).toBeNull();
  });

  it('adds the scoped bearer token to tenant API requests', () => {
    const auth = TestBed.inject(AuthService);
    login(auth);
    selectCompany(auth);

    TestBed.inject(HttpClient).get('/api/v1/users').subscribe();
    const request = httpTesting.expectOne('/api/v1/users');
    expect(request.request.headers.get('Authorization')).toBe('Bearer scoped-company-1');
    request.flush({});
  });

  it('re-scopes once on a 401 mid-session, then retries with the fresh scoped token', () => {
    const auth = TestBed.inject(AuthService);
    login(auth);
    selectCompany(auth);

    TestBed.inject(HttpClient).get('/api/v1/users').subscribe();

    // First (revoked) attempt.
    const first = httpTesting.expectOne('/api/v1/users');
    expect(first.request.headers.get('Authorization')).toBe('Bearer scoped-company-1');
    first.flush({}, { status: 401, statusText: 'Unauthorized' });

    // Re-scope issues a fresh scoped token.
    const rescope = httpTesting.expectOne('/api/v1/auth/select-company');
    expect(rescope.request.headers.get('Authorization')).toBe('Bearer token-123');
    rescope.flush({
      accessToken: 'scoped-refreshed',
      tokenType: 'Bearer',
      expiresIn: 1800,
      activeCompanyId: 'company-1',
      roles: [{ id: 'r-admin', code: 'ADMIN' }],
      effectivePermissions: ['user:write'],
    });

    // One retried request with the new token.
    const retry = httpTesting.expectOne('/api/v1/users');
    expect(retry.request.headers.get('Authorization')).toBe('Bearer scoped-refreshed');
    retry.flush([]);

    expect(auth.authorizationHeader()).toBe('Bearer scoped-refreshed');
    expect(auth.isAuthenticated()).toBe(true);
  });

  it('logs out when re-scope cannot find an active company after a 401', () => {
    const router = TestBed.inject(Router);
    const auth = TestBed.inject(AuthService);
    const navigateByUrl = vi.spyOn(router, 'navigateByUrl');
    login(auth);
    // No scoped token / active company selected yet.

    TestBed.inject(HttpClient)
      .get('/api/v1/users')
      .subscribe({ error: () => undefined });

    const request = httpTesting.expectOne('/api/v1/users');
    request.flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(auth.session()).toBeNull();
    expect(auth.isAuthenticated()).toBe(false);
    expect(localStorage.getItem(storageKey)).toBeNull();
    expect(navigateByUrl).toHaveBeenCalledWith('/login');
  });

  it('guards authenticated, guest, driver, and office routes by role code', () => {
    const router = TestBed.inject(Router);
    const auth = TestBed.inject(AuthService);

    const unauthenticated = TestBed.runInInjectionContext(() =>
      authGuard({} as never, {} as never),
    );
    expect(unauthenticated.toString()).toBe(router.createUrlTree(['/login']).toString());

    login(auth);
    const authenticated = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
    const unscoped = TestBed.runInInjectionContext(() =>
      activeCompanyGuard({} as never, { url: '/dashboard' } as never),
    );
    const guest = TestBed.runInInjectionContext(() => guestGuard({} as never, {} as never));

    expect(authenticated).toBe(true);
    expect(unscoped.toString()).toBe(
      router.createUrlTree(['/select-company'], { queryParams: { returnUrl: '/dashboard' } }).toString(),
    );
    expect(guest.toString()).toBe(router.createUrlTree(['/select-company']).toString());

    selectCompany(auth);

    const scoped = TestBed.runInInjectionContext(() =>
      activeCompanyGuard({} as never, { url: '/dashboard' } as never),
    );
    const scopedGuest = TestBed.runInInjectionContext(() => guestGuard({} as never, {} as never));
    const office = TestBed.runInInjectionContext(() => officeGuard({} as never, {} as never));
    expect(scoped).toBe(true);
    expect(scopedGuest.toString()).toBe(router.createUrlTree(['/dashboard']).toString());
    expect(office).toBe(true);
  });

  it('routes driver and office guards by role code set', () => {
    const router = TestBed.inject(Router);
    const auth = TestBed.inject(AuthService);

    auth.login({ email: 'driver@example.com', password: 'password123' }).subscribe();
    httpTesting.expectOne('/api/v1/auth/login').flush({
      accessToken: 'global-token',
      user: { id: 'driver-user', email: 'driver@example.com' },
      availableCompanies: [
        { companyId: 'company-1', tradeName: 'ACME', roles: [{ id: 'r-driver', code: 'DRIVER' }] },
      ],
      scopedToken: {
        accessToken: 'driver-scoped-token',
        activeCompanyId: 'company-1',
        roles: [{ id: 'r-driver', code: 'DRIVER' }],
        effectivePermissions: [],
      },
    });

    const driver = TestBed.runInInjectionContext(() => driverGuard({} as never, {} as never));
    const office = TestBed.runInInjectionContext(() => officeGuard({} as never, {} as never));

    expect(driver).toBe(true);
    expect(office.toString()).toBe(router.createUrlTree(['/driver/shipments']).toString());
  });
});