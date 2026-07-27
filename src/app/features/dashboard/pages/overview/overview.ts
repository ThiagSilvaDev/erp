import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormField, form, submit, validate } from '@angular/forms/signals';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { Subject, catchError, map, of, startWith, switchMap, tap } from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzSkeletonModule } from 'ng-zorro-antd/skeleton';

import type {
  GetExecutiveKpisBucket,
  GetExecutiveKpisParams,
} from '@generated-api/modules/kpis/dtos';
import { KpiMetricCard } from '../../components/kpi-metric-card/kpi-metric-card';
import { KpiTrendChart } from '../../components/kpi-trend-chart/kpi-trend-chart';
import { ExecutiveKpiFacade } from '../../data-access/executive-kpi.facade';
import {
  ExecutiveKpiViewModel,
  KpiBucket,
  KpiFilterModel,
  KpiLoadError,
} from '../../models/executive-kpi-view-model';
import { toKpiLoadError } from '../../utils/kpi-errors';
import { inclusiveDays, isIsoDate } from '../../utils/kpi-formatters';

interface ParsedKpiQuery {
  model: KpiFilterModel;
  params?: GetExecutiveKpisParams;
  error?: KpiLoadError;
}

type KpiPageState =
  | { status: 'loading' }
  | { status: 'success'; data: ExecutiveKpiViewModel }
  | { status: 'error'; error: KpiLoadError };

@Component({
  selector: 'app-overview',
  imports: [
    FormField,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzSkeletonModule,
    KpiMetricCard,
    KpiTrendChart,
  ],
  templateUrl: './overview.html',
  styleUrl: './overview.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Overview {
  private readonly destroyRef = inject(DestroyRef);
  private readonly facade = inject(ExecutiveKpiFacade);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly reload = new Subject<void>();

  readonly filterModel = signal<KpiFilterModel>({
    from: '',
    to: '',
    bucket: 'DAY',
  });
  readonly filterForm = form(this.filterModel, (schema) => {
    validate(schema.from, ({ valueOf }) => {
      const message = validateKpiFilter({
        from: valueOf(schema.from),
        to: valueOf(schema.to),
        bucket: valueOf(schema.bucket),
      });
      return message ? { kind: 'period', message } : undefined;
    });
  });

  readonly filterError = computed(() => validateKpiFilter(this.filterModel()));
  readonly isLoading = signal(true);
  readonly data = signal<ExecutiveKpiViewModel | null>(null);
  readonly error = signal<KpiLoadError | null>(null);

  constructor() {
    this.route.queryParamMap
      .pipe(
        map(parseKpiQuery),
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
      await this.router.navigate([], {
        relativeTo: this.route,
        queryParams: {
          from: model.from || null,
          to: model.to || null,
          bucket: model.bucket,
        },
      });
    });
  }

  resetFilters(): void {
    this.filterModel.set({ from: '', to: '', bucket: 'DAY' });
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { from: null, to: null, bucket: null },
    });
  }

  retry(): void {
    this.reload.next();
  }

  errorTitle(error: KpiLoadError): string {
    return error.kind === 'forbidden' ? 'Access denied' : 'Dashboard unavailable';
  }

  private loadState(parsed: ParsedKpiQuery) {
    if (parsed.error) {
      return of<KpiPageState>({ status: 'error', error: parsed.error });
    }

    return this.facade.load(parsed.params).pipe(
      map((data): KpiPageState => ({ status: 'success', data })),
      startWith<KpiPageState>({ status: 'loading' }),
      catchError((error: unknown) =>
        of<KpiPageState>({ status: 'error', error: toKpiLoadError(error) }),
      ),
    );
  }

  private applyState(state: KpiPageState): void {
    this.isLoading.set(state.status === 'loading');

    if (state.status === 'loading') {
      this.error.set(null);
      return;
    }

    if (state.status === 'error') {
      this.data.set(null);
      this.error.set(state.error);
      return;
    }

    this.error.set(null);
    this.data.set(state.data);
    this.filterModel.set({
      from: state.data.period.from,
      to: state.data.period.to,
      bucket: state.data.period.bucket,
    });
  }
}

export function parseKpiQuery(params: ParamMap): ParsedKpiQuery {
  const from = params.get('from')?.trim() ?? '';
  const to = params.get('to')?.trim() ?? '';
  const rawBucket = params.get('bucket')?.trim().toUpperCase() ?? '';
  const bucket: KpiBucket = isKpiBucket(rawBucket) ? rawBucket : 'DAY';
  const model = { from, to, bucket };

  if (rawBucket && !isKpiBucket(rawBucket)) {
    return {
      model,
      error: clientValidationError('Bucket must be DAY, WEEK, or MONTH.'),
    };
  }

  const validationMessage = validateKpiFilter(model);
  if (validationMessage) {
    return { model, error: clientValidationError(validationMessage) };
  }

  const generatedBucket = bucket as GetExecutiveKpisBucket;
  const hasQuery = Boolean(from || to || rawBucket);
  return {
    model,
    params: hasQuery
      ? {
          from: from || undefined,
          to: to || undefined,
          bucket: rawBucket ? generatedBucket : undefined,
        }
      : undefined,
  };
}

export function validateKpiFilter(model: KpiFilterModel): string | null {
  if (Boolean(model.from) !== Boolean(model.to)) {
    return 'Start and end dates must be provided together.';
  }

  if (!model.from && !model.to) {
    return null;
  }

  if (!isIsoDate(model.from) || !isIsoDate(model.to)) {
    return 'Enter valid dates in YYYY-MM-DD format.';
  }

  const days = inclusiveDays(model.from, model.to);
  if (days < 1) {
    return 'The start date must be on or before the end date.';
  }
  if (days > 366) {
    return 'The selected interval cannot exceed 366 days.';
  }
  return null;
}

function isKpiBucket(value: string): value is KpiBucket {
  return value === 'DAY' || value === 'WEEK' || value === 'MONTH';
}

function clientValidationError(message: string): KpiLoadError {
  return {
    kind: 'validation',
    message,
    details: ['Update the filters and apply them again.'],
  };
}
