import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { NzMenuModule } from 'ng-zorro-antd/menu';

import { DashboardShellStateService } from './dashboard-shell-state.service';

interface MenuItem {
  label: string;
  path: string;
  icon: string;
}

interface SubmenuGroup {
  title: string;
  icon: string;
  items: MenuItem[];
}

@Component({
  selector: 'app-shell',
  imports: [
    RouterLink,
    RouterOutlet,
    NzButtonModule,
    NzDropDownModule,
    NzIconModule,
    NzLayoutModule,
    NzMenuModule,
  ],
  templateUrl: './dashboard-shell.html',
  styleUrl: './dashboard-shell.css',
  providers: [DashboardShellStateService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardShell {
  readonly state = inject(DashboardShellStateService);

  readonly primaryItems: MenuItem[] = [
    { label: 'Dashboard', path: '/dashboard', icon: 'dashboard' },
  ];

  readonly submenuGroups: SubmenuGroup[] = [
    {
      title: 'Catalog',
      icon: 'appstore',
      items: [
        { label: 'Categories', path: '/categories', icon: 'appstore' },
        { label: 'Products', path: '/products', icon: 'shop' },
      ],
    },
    {
      title: 'Contacts',
      icon: 'contacts',
      items: [
        { label: 'Customers', path: '/customers', icon: 'user' },
        { label: 'Suppliers', path: '/suppliers', icon: 'contacts' },
        { label: 'Company contacts', path: '/company-contacts', icon: 'contacts' },
        { label: 'Contact persons', path: '/contact-persons', icon: 'team' },
      ],
    },
    {
      title: 'Sales',
      icon: 'shopping-cart',
      items: [
        { label: 'PDV', path: '/pdv', icon: 'credit-card' },
        { label: 'Sales orders', path: '/sales-orders', icon: 'shopping-cart' },
      ],
    },
    {
      title: 'Logistics',
      icon: 'truck',
      items: [
        { label: 'Vehicles', path: '/logistics/vehicles', icon: 'truck' },
        { label: 'Drivers', path: '/logistics/drivers', icon: 'user' },
        { label: 'Shipments', path: '/logistics/shipments', icon: 'container' },
      ],
    },
    {
      title: 'Inventory',
      icon: 'database',
      items: [
        { label: 'Warehouses', path: '/warehouses', icon: 'inbox' },
        { label: 'Inventory items', path: '/inventory-items', icon: 'database' },
        { label: 'Stock movements', path: '/stock-movements', icon: 'file-text' },
        { label: 'Reservations', path: '/reservations', icon: 'container' },
      ],
    },
    {
      title: 'Financial',
      icon: 'wallet',
      items: [
        { label: 'Invoices', path: '/invoices', icon: 'wallet' },
        { label: 'Payments', path: '/payments', icon: 'dollar' },
        { label: 'Financial entries', path: '/financial-entries', icon: 'file-text' },
      ],
    },
    {
      title: 'Admin',
      icon: 'apartment',
      items: [
        { label: 'Companies', path: '/companies', icon: 'apartment' },
        { label: 'Users', path: '/users', icon: 'team' },
      ],
    },
  ];

  toggleSidebar(): void {
    this.state.toggleSidebar();
  }

  logout(): void {
    this.state.logout();
  }
}
