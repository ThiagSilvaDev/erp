import { Routes } from '@angular/router';

import {
  activeCompanyGuard,
  authGuard,
  driverGuard,
  guestGuard,
  officeGuard,
} from '@core/auth/auth.guard';
import { DashboardShell } from '@features/dashboard/layout/dashboard-shell';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadChildren: () => import('@features/auth/auth.routes').then((m) => m.AUTH_ROUTES),
  },
  {
    path: 'select-company',
    canActivate: [authGuard],
    loadComponent: () =>
      import('@features/auth/pages/select-company/select-company').then((m) => m.SelectCompany),
  },
  {
    path: '',
    component: DashboardShell,
    canActivate: [authGuard, activeCompanyGuard],
    children: [
      {
        path: 'driver',
        canActivate: [driverGuard],
        loadChildren: () => import('@features/driver/driver.routes').then((m) => m.DRIVER_ROUTES),
      },
      {
        path: '',
        canActivate: [officeGuard],
        children: [
          { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
          {
            path: 'dashboard',
            loadChildren: () =>
              import('@features/dashboard/dashboard.routes').then((m) => m.DASHBOARD_ROUTES),
          },
          {
            path: '',
            loadChildren: () => import('@features/erp/erp.routes').then((m) => m.ERP_ROUTES),
          },
        ],
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
