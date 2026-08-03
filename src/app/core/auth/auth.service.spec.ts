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

  function login(auth: AuthService): void {
    auth.login({ email: 'admin@example.com', password: 'password123' }).subscribe();

    const request = httpTesting.expectOne('/api/v1/auth/login');
    expect(request.request.method).toBe('POST');
    request.flush({
      accessToken: 'token-123',
      tokenType: 'Bearer',
      expiresIn: 3600,
      user: { id: '1', email: 'admin@example.com', role: 'ADMIN' },
      availableCompanies: [
        { companyId: 'company-1', role: 'ADMIN' },
        { companyId: 'company-2', role: 'MANAGER' },
      ],
    });
  }

  function selectCompany(auth: AuthService): void {
    auth
      .selectCompany('company-1', {
        id: 'company-1',
        name: 'ACME',
        companyCode: 101,
      })
      .subscribe();

    const request = httpTesting.expectOne('/api/v1/auth/select-company');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ companyId: 'company-1' });
    expect(request.request.headers.get('Authorization')).toBe('Bearer token-123');
    request.flush({
      accessToken: 'scoped-token-123',
      tokenType: 'Bearer',
      expiresIn: 1800,
      activeCompanyId: 'company-1',
      role: 'ADMIN',
    });
  }

  it('starts without a persisted session', () => {
    const auth = TestBed.inject(AuthService);

    expect(auth.session()).toBeNull();
    expect(auth.isAuthenticated()).toBe(false);
    expect(auth.authorizationHeader()).toBeNull();
  });

  it('stores a successful login session in signal state and local storage', () => {
    const auth = TestBed.inject(AuthService);
    const now = Date.now();

    login(auth);

    expect(auth.isAuthenticated()).toBe(true);
    expect(auth.authorizationHeader()).toBe('Bearer token-123');
    expect(auth.hasActiveCompany()).toBe(false);
    expect(auth.session()).toEqual(
      expect.objectContaining({
        accessToken: 'token-123',
        tokenType: 'Bearer',
        globalAccessToken: 'token-123',
        globalTokenType: 'Bearer',
        expiresIn: 3600,
        user: { id: '1', email: 'admin@example.com', role: 'ADMIN' },
        availableCompanies: [
          { companyId: 'company-1', role: 'ADMIN' },
          { companyId: 'company-2', role: 'MANAGER' },
        ],
      }),
    );
    expect(auth.session()?.expiresAt).toBeGreaterThanOrEqual(now + 3600 * 1000);

    const storedSession = JSON.parse(localStorage.getItem(storageKey) ?? '{}');
    expect(storedSession).toEqual(
      expect.objectContaining({
        accessToken: 'token-123',
        tokenType: 'Bearer',
        globalAccessToken: 'token-123',
        globalTokenType: 'Bearer',
        expiresIn: 3600,
        user: { id: '1', email: 'admin@example.com', role: 'ADMIN' },
        availableCompanies: [
          { companyId: 'company-1', role: 'ADMIN' },
          { companyId: 'company-2', role: 'MANAGER' },
        ],
      }),
    );
    expect(storedSession.expiresAt).toBeGreaterThanOrEqual(now + 3600 * 1000);
  });

  it('activates a scoped token, company, and role directly from login', () => {
    const auth = TestBed.inject(AuthService);

    auth.login({ email: 'driver@example.com', password: 'password123' }).subscribe();
    const request = httpTesting.expectOne('/api/v1/auth/login');
    request.flush({
      accessToken: 'global-token',
      tokenType: 'Bearer',
      expiresIn: 3600,
      user: { id: 'driver-user', email: 'driver@example.com', role: 'DRIVER' },
      availableCompanies: [
        {
          companyId: 'company-1',
          tradeName: 'ACME',
          cnpj: '00.000.000/0001-00',
          role: 'DRIVER',
        },
      ],
      scopedToken: {
        accessToken: 'driver-scoped-token',
        tokenType: 'Bearer',
        expiresIn: 1800,
        activeCompanyId: 'company-1',
        role: 'DRIVER',
      },
    });

    expect(auth.authorizationHeader()).toBe('Bearer driver-scoped-token');
    expect(auth.globalAuthorizationHeader()).toBe('Bearer global-token');
    expect(auth.session()?.activeCompany).toEqual({ id: 'company-1', name: 'ACME' });
    expect(auth.role()).toBe('DRIVER');
    expect(auth.isDriver()).toBe(true);
    expect(auth.landingPath()).toBe('/driver/shipments');
  });

  it('stores the selected company and scoped token', () => {
    const auth = TestBed.inject(AuthService);

    login(auth);
    selectCompany(auth);

    expect(auth.hasActiveCompany()).toBe(true);
    expect(auth.authorizationHeader()).toBe('Bearer scoped-token-123');
    expect(auth.session()).toEqual(
      expect.objectContaining({
        accessToken: 'scoped-token-123',
        globalAccessToken: 'token-123',
        activeCompany: { id: 'company-1', name: 'ACME', companyCode: 101 },
        role: 'ADMIN',
      }),
    );
  });

  it('uses the global login token when switching companies after a scoped token is active', () => {
    const auth = TestBed.inject(AuthService);

    login(auth);
    selectCompany(auth);

    auth
      .selectCompany('company-2', {
        id: 'company-2',
        name: 'Beta',
      })
      .subscribe();

    const request = httpTesting.expectOne('/api/v1/auth/select-company');
    expect(request.request.body).toEqual({ companyId: 'company-2' });
    expect(request.request.headers.get('Authorization')).toBe('Bearer token-123');
    request.flush({
      accessToken: 'scoped-token-456',
      tokenType: 'Bearer',
      expiresIn: 1800,
      activeCompanyId: 'company-2',
      role: 'MANAGER',
    });

    expect(auth.authorizationHeader()).toBe('Bearer scoped-token-456');
    expect(auth.globalAuthorizationHeader()).toBe('Bearer token-123');
    expect(auth.session()).toEqual(
      expect.objectContaining({
        accessToken: 'scoped-token-456',
        globalAccessToken: 'token-123',
        activeCompany: { id: 'company-2', name: 'Beta' },
        role: 'MANAGER',
      }),
    );
  });

  it('restores a persisted session after the app reloads', () => {
    localStorage.setItem(
      storageKey,
      JSON.stringify({
        accessToken: 'token-123',
        tokenType: 'Bearer',
        globalAccessToken: 'token-123',
        globalTokenType: 'Bearer',
        expiresIn: 3600,
        expiresAt: Date.now() + 3600 * 1000,
        user: { id: '1', email: 'admin@example.com', role: 'ADMIN' },
        role: 'ADMIN',
        availableCompanies: [{ companyId: 'company-1', role: 'ADMIN' }],
        activeCompany: { id: 'company-1', name: 'ACME', companyCode: 101 },
      }),
    );

    const auth = TestBed.inject(AuthService);

    expect(auth.isAuthenticated()).toBe(true);
    expect(auth.hasActiveCompany()).toBe(true);
    expect(auth.authorizationHeader()).toBe('Bearer token-123');
    expect(auth.globalAuthorizationHeader()).toBe('Bearer token-123');
  });

  it('does not restore an expired persisted session', () => {
    localStorage.setItem(
      storageKey,
      JSON.stringify({
        accessToken: 'token-123',
        tokenType: 'Bearer',
        expiresIn: 3600,
        expiresAt: Date.now() - 1000,
        user: { id: '1', email: 'admin@example.com', role: 'ADMIN' },
      }),
    );

    const auth = TestBed.inject(AuthService);

    expect(auth.session()).toBeNull();
    expect(auth.isAuthenticated()).toBe(false);
    expect(auth.authorizationHeader()).toBeNull();
    expect(localStorage.getItem(storageKey)).toBeNull();
  });

  it('adds the bearer token to API requests', () => {
    const auth = TestBed.inject(AuthService);
    login(auth);

    TestBed.inject(HttpClient).get('/api/v1/users').subscribe();

    const request = httpTesting.expectOne('/api/v1/users');
    expect(request.request.headers.get('Authorization')).toBe('Bearer token-123');
    request.flush({});
  });

  it('logs out and redirects to login when an API request returns unauthorized', () => {
    const router = TestBed.inject(Router);
    const auth = TestBed.inject(AuthService);
    const navigateByUrl = vi.spyOn(router, 'navigateByUrl');
    login(auth);

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

  it('guards authenticated and guest routes', () => {
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
      router
        .createUrlTree(['/select-company'], { queryParams: { returnUrl: '/dashboard' } })
        .toString(),
    );
    expect(guest.toString()).toBe(router.createUrlTree(['/select-company']).toString());

    selectCompany(auth);

    const scoped = TestBed.runInInjectionContext(() =>
      activeCompanyGuard({} as never, { url: '/dashboard' } as never),
    );
    const scopedGuest = TestBed.runInInjectionContext(() => guestGuard({} as never, {} as never));
    expect(scoped).toBe(true);
    expect(scopedGuest.toString()).toBe(router.createUrlTree(['/dashboard']).toString());
  });

  it('separates driver and office routes by scoped role', () => {
    const router = TestBed.inject(Router);
    const auth = TestBed.inject(AuthService);

    auth.login({ email: 'driver@example.com', password: 'password123' }).subscribe();
    httpTesting.expectOne('/api/v1/auth/login').flush({
      accessToken: 'global-token',
      user: { id: 'driver-user', email: 'driver@example.com', role: 'DRIVER' },
      availableCompanies: [{ companyId: 'company-1', tradeName: 'ACME', role: 'DRIVER' }],
      scopedToken: {
        accessToken: 'driver-scoped-token',
        activeCompanyId: 'company-1',
        role: 'DRIVER',
      },
    });

    const driver = TestBed.runInInjectionContext(() => driverGuard({} as never, {} as never));
    const office = TestBed.runInInjectionContext(() => officeGuard({} as never, {} as never));

    expect(driver).toBe(true);
    expect(office.toString()).toBe(router.createUrlTree(['/driver/shipments']).toString());
  });
});
