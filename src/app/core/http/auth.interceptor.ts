import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../auth/auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const authService = inject(AuthService);
  const isApiRequest = request.url.startsWith('/api/');
  const isLoginRequest = request.url.startsWith('/api/v1/auth/login');
  const isSelectCompanyRequest = request.url.startsWith('/api/v1/auth/select-company');
  const authorization = isSelectCompanyRequest
    ? authService.globalAuthorizationHeader()
    : authService.authorizationHeader();
  const authorizedRequest =
    authorization && isApiRequest
      ? request.clone({
          setHeaders: { Authorization: authorization },
        })
      : request;

  return next(authorizedRequest).pipe(
    catchError((error: unknown) => {
      if (isExpiredAuthResponse(error, isApiRequest, isLoginRequest)) {
        authService.logout();
      }

      return throwError(() => error);
    }),
  );
};

function isExpiredAuthResponse(
  error: unknown,
  isApiRequest: boolean,
  isLoginRequest: boolean,
): boolean {
  return (
    isApiRequest &&
    !isLoginRequest &&
    error instanceof HttpErrorResponse &&
    error.status === 401
  );
}
