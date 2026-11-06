import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { NzCardModule } from 'ng-zorro-antd/card';

import { KpiChartSeriesViewModel, KpiChartViewModel } from '../../models/executive-kpi-view-model';
import { formatHours, formatMoney, formatNumber } from '../../utils/kpi-formatters';

interface ChartPoint {
  index: number;
  x: number;
  y: number;
  value: number;
}

interface RenderedSeries {
  series: KpiChartSeriesViewModel;
  points: ChartPoint[];
  segments: string[];
}

interface AxisTick {
  value: number;
  y: number;
  label: string;
}

interface XAxisTick {
  index: number;
  x: number;
  label: string;
}

@Component({
  selector: 'app-kpi-trend-chart',
  imports: [NzCardModule],
  templateUrl: './kpi-trend-chart.html',
  styleUrl: './kpi-trend-chart.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KpiTrendChart {
  private readonly width = 760;
  private readonly height = 260;
  private readonly left = 58;
  private readonly right = 18;
  private readonly top = 18;
  private readonly bottom = 42;

  readonly chart = input.required<KpiChartViewModel>();

  readonly viewBox = `0 0 ${this.width} ${this.height}`;
  readonly plotWidth = this.width - this.left - this.right;
  readonly plotHeight = this.height - this.top - this.bottom;
  readonly baselineY = this.top + this.plotHeight;

  readonly maxValue = computed(() => {
    const values = this.chart().series.flatMap((series) =>
      series.values.filter((value): value is number => value !== null),
    );
    return Math.max(1, ...values);
  });

  readonly yTicks = computed<AxisTick[]>(() =>
    [0, 0.25, 0.5, 0.75, 1].map((ratio) => {
      const value = this.maxValue() * ratio;
      return {
        value,
        y: this.baselineY - this.plotHeight * ratio,
        label: this.formatAxisValue(value),
      };
    }),
  );

  readonly xTicks = computed<XAxisTick[]>(() => {
    const buckets = this.chart().buckets;
    if (!buckets.length) {
      return [];
    }

    const last = buckets.length - 1;
    const indexes = new Set(
      Array.from({ length: Math.min(6, buckets.length) }, (_, index) =>
        Math.round((index * last) / Math.max(1, Math.min(6, buckets.length) - 1)),
      ),
    );

    return [...indexes].map((index) => ({
      index,
      x: this.xFor(index, buckets.length),
      label: buckets[index]?.label ?? '',
    }));
  });

  readonly renderedSeries = computed<RenderedSeries[]>(() =>
    this.chart().series.map((series) => {
      const points = series.values.flatMap((value, index) =>
        value === null
          ? []
          : [
              {
                index,
                value,
                x: this.xFor(index, this.chart().buckets.length),
                y: this.yFor(value),
              },
            ],
      );

      return {
        series,
        points,
        segments: this.segments(series),
      };
    }),
  );

  formatValue(value: number | null): string {
    if (value === null) {
      return '—';
    }

    const chart = this.chart();
    if (chart.format === 'currency') {
      return formatMoney(value, chart.currencyCode ?? '');
    }
    if (chart.format === 'hours') {
      return formatHours(value);
    }
    return formatNumber(value);
  }

  pointLabel(rendered: RenderedSeries, point: ChartPoint): string {
    const bucket = this.chart().buckets[point.index];
    return `${rendered.series.label}, ${bucket?.accessibleLabel ?? 'unknown period'}: ${this.formatValue(point.value)}`;
  }

  private segments(series: KpiChartSeriesViewModel): string[] {
    const segments: string[] = [];
    let current: string[] = [];

    series.values.forEach((value, index) => {
      if (value === null) {
        if (current.length) {
          segments.push(current.join(' '));
          current = [];
        }
        return;
      }
      current.push(`${this.xFor(index, this.chart().buckets.length)},${this.yFor(value)}`);
    });

    if (current.length) {
      segments.push(current.join(' '));
    }
    return segments;
  }

  private xFor(index: number, count: number): number {
    return this.left + (index * this.plotWidth) / Math.max(1, count - 1);
  }

  private yFor(value: number): number {
    return this.baselineY - (value / this.maxValue()) * this.plotHeight;
  }

  private formatAxisValue(value: number): string {
    if (this.chart().format === 'currency' && value >= 1_000_000) {
      return `${formatNumber(value / 1_000_000)}M`;
    }
    if (value >= 1_000) {
      return `${formatNumber(value / 1_000)}k`;
    }
    return formatNumber(value);
  }
}
