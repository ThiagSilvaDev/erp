import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { AuthStateService } from '../auth/auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const authorization = inject(AuthStateService).authorizationHeader();
  const isApiRequest = request.url.startsWith('/api/');

  if (!authorization || !isApiRequest) {
    return next(request);
  }

  return next(
    request.clone({
      setHeaders: { Authorization: authorization },
    }),
  );
};
