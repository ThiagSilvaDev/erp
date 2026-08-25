import { HttpContextToken, HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';

import { AuthService } from '../auth/auth.service';

export const REVOKED_RETRY = new HttpContextToken<boolean>(() => false);

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const authService = inject(AuthService);

  if (!request.url.startsWith('/api/')) {
    return next(request);
  }

  return next(attachAuthorization(request, authService)).pipe(
    catchError((error: unknown) => {
      if (shouldRescope(error, request)) {
        return authService.rescope(request.url).pipe(
          switchMap(() => next(retryRequest(request, authService))),
          catchError((rescopeError: unknown) => {
            // A 403 during re-scope is a permission denial (e.g. a tenant user
            // hitting a platform-only endpoint, or a genuinely revoked scope).
            // Surface the original error without killing the session so callers
            // like option loaders can degrade gracefully.
            const isPermissionDenial =
              rescopeError instanceof HttpErrorResponse && rescopeError.status === 403;
            if (!isPermissionDenial) {
              authService.logout();
            }
            return throwError(() => (isPermissionDenial ? error : rescopeError));
          }),
        );
      }

      return throwError(() => error);
    }),
  );
};

function shouldRescope(error: unknown, request: HttpRequest<unknown>): boolean {
  return (
    error instanceof HttpErrorResponse &&
    (error.status === 401 || error.status === 403) &&
    !request.context.get(REVOKED_RETRY) &&
    !request.url.startsWith('/api/v1/auth/')
  );
}

function attachAuthorization(request: HttpRequest<unknown>, authService: AuthService): HttpRequest<unknown> {
  const header = authService.headerForUrl(request.url);
  return header ? request.clone({ setHeaders: { Authorization: header } }) : request;
}

function retryRequest(request: HttpRequest<unknown>, authService: AuthService): HttpRequest<unknown> {
  const header = authService.headerForUrl(request.url);
  return request.clone({
    setHeaders: header ? { Authorization: header } : {},
    context: request.context.set(REVOKED_RETRY, true),
  });
}