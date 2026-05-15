import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { authGuard, guestGuard } from './auth.guard';
import { AuthStateService } from './auth.service';
import { authInterceptor } from '../http/auth.interceptor';

describe('AuthStateService', () => {
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();

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
    localStorage.clear();
  });

  it('stores a successful login session', () => {
    const auth = TestBed.inject(AuthStateService);

    auth.login({ email: 'admin@example.com', password: 'password123' }).subscribe();

    const request = httpTesting.expectOne('/api/v1/auth/login');
    expect(request.request.method).toBe('POST');
    request.flush({
      accessToken: 'token-123',
      tokenType: 'Bearer',
      expiresIn: 3600,
      user: { id: '1', email: 'admin@example.com', role: 'ADMIN' },
    });

    expect(auth.isAuthenticated()).toBe(true);
    expect(auth.authorizationHeader()).toBe('Bearer token-123');
    expect(localStorage.getItem('erp.session')).toContain('token-123');
  });

  it('adds the bearer token to API requests', () => {
    localStorage.setItem(
      'erp.session',
      JSON.stringify({ accessToken: 'token-123', tokenType: 'Bearer' }),
    );

    TestBed.inject(AuthStateService);
    TestBed.inject(HttpClient).get('/api/v1/users').subscribe();

    const request = httpTesting.expectOne('/api/v1/users');
    expect(request.request.headers.get('Authorization')).toBe('Bearer token-123');
    request.flush({});
  });

  it('guards authenticated and guest routes', () => {
    const router = TestBed.inject(Router);
    const auth = TestBed.inject(AuthStateService);

    const unauthenticated = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
    expect(unauthenticated.toString()).toBe(router.createUrlTree(['/login']).toString());

    auth.login({ email: 'admin@example.com', password: 'password123' }).subscribe();
    httpTesting.expectOne('/api/v1/auth/login').flush({
      accessToken: 'token-123',
      tokenType: 'Bearer',
      user: { email: 'admin@example.com' },
    });

    const authenticated = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
    const guest = TestBed.runInInjectionContext(() => guestGuard({} as never, {} as never));

    expect(authenticated).toBe(true);
    expect(guest.toString()).toBe(router.createUrlTree(['/dashboard']).toString());
  });
});
