import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzSpinModule } from 'ng-zorro-antd/spin';

import { AuthService } from '@core/auth/auth.service';
import { appPaths } from '@core/routing/app-paths';
import type { AuthCompanyResponse } from '@generated-api/modules/authentication/dtos';

interface CompanyChoice extends AuthCompanyResponse {
  legalName?: string;
  tradeName?: string;
  cnpj?: string;
}

@Component({
  selector: 'app-select-company',
  imports: [NzAlertModule, NzButtonModule, NzCardModule, NzIconModule, NzSpinModule],
  templateUrl: './select-company.html',
  styleUrl: './select-company.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SelectCompany {
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly companies = computed(() => this.auth.availableCompanies() as CompanyChoice[]);
  readonly isLoading = signal(false);
  readonly selectedCompanyId = signal<string | null>(null);
  readonly isEnteringPlatform = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly activeCompanyId = computed(() => this.auth.session()?.activeCompany?.id ?? null);
  readonly canActAsPlatformAdmin = computed(() => this.auth.canActAsPlatformAdmin());

  selectCompany(company: CompanyChoice): void {
    const companyId = company.companyId;

    if (!companyId || this.selectedCompanyId()) {
      return;
    }

    this.selectedCompanyId.set(companyId);
    this.errorMessage.set(null);

    this.auth
      .selectCompany(companyId, {
        id: companyId,
        name: this.companyName(company),
        companyCode: undefined,
      })
      .pipe(
        finalize(() => this.selectedCompanyId.set(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => void this.router.navigateByUrl(this.nextPath()),
        error: () => {
          this.errorMessage.set('Unable to select this company.');
        },
      });
  }

  companyName(company: CompanyChoice): string {
    return company.legalName || company.tradeName || 'Company';
  }

  companyDocument(company: CompanyChoice): string {
    return company.cnpj ?? this.rolesLabel(company) ?? 'No CNPJ';
  }

  rolesLabel(company: CompanyChoice): string | undefined {
    const codes = (company.roles ?? [])
      .map((role) => role.code)
      .filter((code): code is string => Boolean(code));
    return codes.length ? codes.join(', ') : undefined;
  }

  enterPlatformAdmin(): void {
    if (this.isEnteringPlatform()) {
      return;
    }

    this.isEnteringPlatform.set(true);
    this.errorMessage.set(null);

    this.auth
      .selectPlatform()
      .pipe(
        finalize(() => this.isEnteringPlatform.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => void this.router.navigateByUrl(appPaths.companies),
        error: () =>
          this.errorMessage.set('Unable to enter platform admin mode. This account may not have platform access.'),
      });
  }

  isSelecting(company: CompanyChoice): boolean {
    return this.selectedCompanyId() === company.companyId;
  }

  isActive(company: CompanyChoice): boolean {
    return Boolean(company.companyId && this.activeCompanyId() === company.companyId);
  }

  trackCompany(_index: number, company: CompanyChoice): string {
    return company.companyId ?? this.companyName(company);
  }

  logout(): void {
    this.auth.logout();
  }

  private nextPath(): string {
    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');

    if (returnUrl && returnUrl !== appPaths.login && returnUrl !== appPaths.selectCompany) {
      return returnUrl;
    }

    return this.auth.landingPath();
  }
}
