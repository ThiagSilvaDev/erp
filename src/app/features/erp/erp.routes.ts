import { Routes } from '@angular/router';

export const ERP_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/entity/entity').then((m) => m.Entity),
  },
];
