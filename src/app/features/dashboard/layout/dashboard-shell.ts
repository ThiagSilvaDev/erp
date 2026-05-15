import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { NzMenuModule } from 'ng-zorro-antd/menu';

import { DashboardShellStateService } from './dashboard-shell-state.service';

interface MenuItem {
  label: string;
  path: string;
  icon: string;
}

@Component({
  selector: 'app-shell',
  imports: [RouterLink, RouterOutlet, NzButtonModule, NzIconModule, NzLayoutModule, NzMenuModule],
  templateUrl: './dashboard-shell.html',
  styleUrl: './dashboard-shell.css',
  providers: [DashboardShellStateService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardShell {
  readonly state = inject(DashboardShellStateService);

  readonly primaryItems: MenuItem[] = [{ label: 'Dashboard', path: '/dashboard', icon: 'dashboard' }];
  readonly organizationItems: MenuItem[] = [
    { label: 'Companies', path: '/companies', icon: 'apartment' },
    { label: 'Users', path: '/users', icon: 'team' },
  ];
  readonly catalogItems: MenuItem[] = [
    { label: 'Categories', path: '/categories', icon: 'appstore' },
    { label: 'Products', path: '/products', icon: 'shop' },
  ];
  readonly contactItems: MenuItem[] = [
    { label: 'Customers', path: '/customers', icon: 'user' },
    { label: 'Suppliers', path: '/suppliers', icon: 'contacts' },
    { label: 'Contact persons', path: '/contact-persons', icon: 'team' },
  ];

  toggleSidebar(): void {
    this.state.toggleSidebar();
  }

  logout(): void {
    this.state.logout();
  }
}
