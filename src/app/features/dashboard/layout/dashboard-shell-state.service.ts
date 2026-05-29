import { Injectable, computed, inject, signal } from '@angular/core';

import { AuthService } from '@core/auth/auth.service';

@Injectable()
export class DashboardShellStateService {
  private readonly auth = inject(AuthService);
  private readonly collapsed = signal(false);

  readonly isCollapsed = this.collapsed.asReadonly();
  readonly userLabel = computed(() => this.auth.session()?.user?.email ?? 'ERP user');
  readonly companyLabel = computed(() => this.auth.session()?.activeCompany?.name ?? 'Company');

  toggleSidebar(): void {
    this.collapsed.update((value) => !value);
  }

  logout(): void {
    this.auth.logout();
  }
}
