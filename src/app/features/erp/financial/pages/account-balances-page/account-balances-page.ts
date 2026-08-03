import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormField, form, pattern, required, submit, validate } from '@angular/forms/signals';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { Subject, catchError, map, of, startWith, switchMap, tap } from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';

import { FinancialService } from '@generated-api/modules/financial/Financial.service';
import type {
  AccountBalanceResponse,
  ListAccountBalancesParams,
} from '@generated-api/modules/financial/dtos';
import { formatMoney, isIsoDate } from '@features/dashboard/utils/kpi-formatters';

export interface AccountBalanceFilterModel {
  from: string;
  to: string;
  currencyCode: string;
}

interface ParsedBalanceQuery {
  model: AccountBalanceFilterModel;
  hasQuery: boolean;
  params?: ListAccountBalancesParams;
  validationMessage?: string;
}

type BalancePageState =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'success'; rows: AccountBalanceResponse[] }
  | { status: 'error'; kind: 'forbidden' | 'validation' | 'generic'; message: string };

@Component({
  selector: 'app-account-balances-page',
  imports: [
    FormField,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzEmptyModule,
    NzTableModule,
    NzTagModule,
  ],
  templateUrl: './account-balances-page.html',
  styleUrl: './account-balances-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountBalancesPage {
  private readonly destroyRef = inject(DestroyRef);
  private readonly financial = inject(FinancialService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly reload = new Subject<void>();

  readonly filterModel = signal<AccountBalanceFilterModel>({
    from: '',
    to: '',
    currencyCode: 'BRL',
  });
  readonly filterForm = form(this.filterModel, (schema) => {
    required(schema.from, { message: 'From date is required.' });
    required(schema.to, { message: 'To date is required.' });
    required(schema.currencyCode, { message: 'Currency is required.' });
    pattern(schema.currencyCode, /^[A-Za-z]{3}$/, {
      message: 'Currency must use a three-letter code.',
    });
    validate(schema.from, ({ valueOf }) => {
      const message = validateBalanceFilters({
        from: valueOf(schema.from),
        to: valueOf(schema.to),
        currencyCode: valueOf(schema.currencyCode),
      });
      return message ? { kind: 'period', message } : undefined;
    });
  });

  readonly filterError = computed(() =>
    this.filterForm().touched() ? validateBalanceFilters(this.filterModel()) : null,
  );
  readonly rows = signal<AccountBalanceResponse[]>([]);
  readonly isLoading = signal(false);
  readonly hasSubmitted = signal(false);
  readonly error = signal<Extract<BalancePageState, { status: 'error' }> | null>(null);

  constructor() {
    this.route.queryParamMap
      .pipe(
        map(parseBalanceQuery),
        tap((parsed) => this.filterModel.set(parsed.model)),
        switchMap((parsed) =>
          this.reload.pipe(
            startWith(undefined),
            switchMap(() => this.loadState(parsed)),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((state) => this.applyState(state));
  }

  applyFilters(event: Event): void {
    event.preventDefault();
    submit(this.filterForm, async () => {
      const model = this.filterModel();
      const currencyCode = model.currencyCode.trim().toUpperCase();
      this.filterModel.set({ ...model, currencyCode });
      const navigated = await this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { from: model.from, to: model.to, currencyCode },
      });
      if (!navigated) {
        this.reload.next();
      }
    });
  }

  retry(): void {
    this.reload.next();
  }

  formattedAmount(row: AccountBalanceResponse): string {
    return formatMoney(row.amount, row.currencyCode ?? this.filterModel().currencyCode);
  }

  errorTitle(): string {
    return this.error()?.kind === 'forbidden' ? 'Access denied' : 'Balances unavailable';
  }

  private loadState(parsed: ParsedBalanceQuery) {
    if (!parsed.hasQuery) {
      return of<BalancePageState>({ status: 'idle' });
    }
    if (parsed.validationMessage || !parsed.params) {
      return of<BalancePageState>({
        status: 'error',
        kind: 'validation',
        message: parsed.validationMessage ?? 'Enter valid balance filters.',
      });
    }

    return this.financial.listAccountBalances(parsed.params).pipe(
      map((rows): BalancePageState => ({ status: 'success', rows })),
      startWith<BalancePageState>({ status: 'loading' }),
      catchError((error: unknown) => of<BalancePageState>(balanceLoadError(error))),
    );
  }

  private applyState(state: BalancePageState): void {
    this.hasSubmitted.set(state.status !== 'idle');
    this.isLoading.set(state.status === 'loading');

    if (state.status === 'loading') {
      this.rows.set([]);
      this.error.set(null);
      return;
    }

    if (state.status === 'success') {
      this.rows.set(state.rows);
      this.error.set(null);
      return;
    }

    if (state.status === 'error') {
      this.rows.set([]);
      this.error.set(state);
      return;
    }

    this.error.set(null);
    if (state.status === 'idle') {
      this.rows.set([]);
    }
  }
}

export function parseBalanceQuery(params: ParamMap): ParsedBalanceQuery {
  const from = params.get('from')?.trim() ?? '';
  const to = params.get('to')?.trim() ?? '';
  const currencyCode = (params.get('currencyCode')?.trim() || 'BRL').toUpperCase();
  const model = { from, to, currencyCode };
  const hasQuery = params.has('from') || params.has('to') || params.has('currencyCode');
  const validationMessage = hasQuery ? validateBalanceFilters(model) : null;

  return {
    model,
    hasQuery,
    params:
      hasQuery && !validationMessage
        ? { from: model.from, to: model.to, currencyCode: model.currencyCode }
        : undefined,
    validationMessage: validationMessage ?? undefined,
  };
}

export function validateBalanceFilters(model: AccountBalanceFilterModel): string | null {
  if (!model.from || !model.to || !model.currencyCode.trim()) {
    return 'From date, to date, and currency are required.';
  }
  if (!isIsoDate(model.from) || !isIsoDate(model.to)) {
    return 'Enter valid dates in YYYY-MM-DD format.';
  }
  if (model.from > model.to) {
    return 'The from date must be on or before the to date.';
  }
  if (!/^[A-Za-z]{3}$/.test(model.currencyCode.trim())) {
    return 'Currency must use a three-letter code.';
  }
  return null;
}

function balanceLoadError(error: unknown): Extract<BalancePageState, { status: 'error' }> {
  if (error instanceof HttpErrorResponse && error.status === 403) {
    return {
      status: 'error',
      kind: 'forbidden',
      message: 'You do not have permission to view account balances.',
    };
  }
  return {
    status: 'error',
    kind: 'generic',
    message: 'Unable to load account balances. Try again in a moment.',
  };
}
