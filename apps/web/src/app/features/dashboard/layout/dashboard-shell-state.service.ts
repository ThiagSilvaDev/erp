import { DestroyRef, Injectable, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { NzMessageService } from 'ng-zorro-antd/message';

import { AuthService } from '@core/auth/auth.service';
import type { AuthCompanyResponse } from '@generated-api/modules/authentication/dtos';
import { UsersService } from '@generated-api/modules/users/Users.service';
import type { UserResponse } from '@generated-api/modules/users/dtos';

interface CompanyOption extends AuthCompanyResponse {
  legalName?: string;
  tradeName?: string;
}

@Injectable()
export class DashboardShellStateService {
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly messages = inject(NzMessageService);
  private readonly users = inject(UsersService);
  private readonly collapsed = signal(false);
  private readonly profileName = signal<string | null>(null);

  readonly isCollapsed = this.collapsed.asReadonly();
  readonly isDriver = computed(() => this.auth.hasRoleCode('DRIVER'));
  readonly userLabel = computed(
    () => this.profileName() ?? this.auth.session()?.user?.email ?? 'ERP user',
  );
  readonly companyLabel = computed(() => this.auth.session()?.activeCompany?.name ?? 'Company');
  readonly companies = computed(() => this.auth.availableCompanies() as CompanyOption[]);
  readonly activeCompanyId = computed(() => this.auth.session()?.activeCompany?.id ?? null);
  readonly switchingCompanyId = signal<string | null>(null);
  readonly canActAsPlatformAdmin = computed(() => this.auth.canActAsPlatformAdmin());

  hasPermission(permission: string): boolean {
    return this.auth.hasPermission(permission);
  }

  hasAnyPermission(permissions: readonly string[]): boolean {
    return this.auth.hasAnyPermission(permissions);
  }

  hasAllPermissions(permissions: readonly string[]): boolean {
    return this.auth.hasAllPermissions(permissions);
  }

  hasRoleCode(code: string): boolean {
    return this.auth.hasRoleCode(code);
  }

  constructor() {
    const userId = this.auth.session()?.user?.id;

    if (userId && !this.auth.hasRoleCode('DRIVER')) {
      this.users
        .getUserById(userId)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: (user) => this.profileName.set(this.displayName(user)),
          error: () => undefined, // Keep the email fallback.
        });
    }
  }

  toggleSidebar(): void {
    this.collapsed.update((value) => !value);
  }

  logout(): void {
    this.auth.logout();
  }

  companyName(company: CompanyOption): string {
    return company.legalName || company.tradeName || 'Company';
  }

  isActiveCompany(company: CompanyOption): boolean {
    return Boolean(company.companyId && this.activeCompanyId() === company.companyId);
  }

  isSwitchingCompany(company: CompanyOption): boolean {
    return this.switchingCompanyId() === company.companyId;
  }

  trackCompany(_index: number, company: CompanyOption): string {
    return company.companyId ?? this.companyName(company);
  }

  switchCompany(company: CompanyOption): void {
    const companyId = company.companyId;

    if (!companyId || this.switchingCompanyId() || this.activeCompanyId() === companyId) {
      return;
    }

    this.switchingCompanyId.set(companyId);

    this.auth
      .selectCompany(companyId, {
        id: companyId,
        name: this.companyName(company),
        companyCode: undefined,
      })
      .pipe(
        finalize(() => this.switchingCompanyId.set(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        // Reload so every page refetches data scoped to the new company.
        next: () => window.location.reload(),
        error: () => this.messages.error('Unable to switch company.'),
      });
  }

  private displayName(user: UserResponse): string | null {
    const fullName = [user.firstName, user.lastName].filter(Boolean).join(' ').trim();

    return user.displayName || fullName || null;
  }
}
