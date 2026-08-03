import { Routes } from '@angular/router';

export const DRIVER_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'shipments' },
  {
    path: 'shipments',
    title: 'My shipments',
    loadComponent: () =>
      import('./pages/driver-shipments-page/driver-shipments-page').then(
        (module) => module.DriverShipmentsPage,
      ),
  },
];
