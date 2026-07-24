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
  readonly errorMessage = signal<string | null>(null);
  readonly activeCompanyId = computed(() => this.auth.session()?.activeCompany?.id ?? null);

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
    return company.tradeName || company.legalName || company.companyId || 'Company';
  }

  companyDocument(company: CompanyChoice): string {
    return company.cnpj ?? company.role ?? 'No CNPJ';
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

    return appPaths.dashboard;
  }
}
