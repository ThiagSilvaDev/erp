import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { appPaths } from '@core/routing/app-paths';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (_route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  return router.createUrlTree([appPaths.login], {
    queryParams: { returnUrl: state.url },
  });
};

export const activeCompanyGuard: CanActivateFn = (_route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.hasActiveCompany()) {
    return true;
  }

  return router.createUrlTree([appPaths.selectCompany], {
    queryParams: { returnUrl: state.url },
  });
};

export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return true;
  }

  if (!authService.hasActiveCompany()) {
    return router.createUrlTree([appPaths.selectCompany]);
  }

  return router.createUrlTree([appPaths.dashboard]);
};
