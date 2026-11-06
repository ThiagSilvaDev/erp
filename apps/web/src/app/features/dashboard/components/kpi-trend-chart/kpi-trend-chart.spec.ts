import { TestBed } from '@angular/core/testing';

import { KpiChartViewModel } from '../../models/executive-kpi-view-model';
import { KpiTrendChart } from './kpi-trend-chart';

describe('KpiTrendChart', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [KpiTrendChart] });
  });

  it('renders focusable points and an accessible data table', async () => {
    const fixture = TestBed.createComponent(KpiTrendChart);
    fixture.componentRef.setInput('chart', chart());
    await fixture.whenStable();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelectorAll('circle[tabindex="0"]')).toHaveLength(5);
    expect(element.querySelector('svg')?.getAttribute('aria-label')).toContain('Order volume');
    expect(element.querySelectorAll('tbody tr')).toHaveLength(3);
  });

  it('breaks a line at null values and safely scales all-zero data', () => {
    const fixture = TestBed.createComponent(KpiTrendChart);
    const value = chart();
    value.series = [{ key: 'zero', label: 'Zero', color: '#000', values: [0, null, 0] }];
    fixture.componentRef.setInput('chart', value);

    expect(fixture.componentInstance.maxValue()).toBe(1);
    expect(fixture.componentInstance.renderedSeries()[0]?.segments).toHaveLength(2);
  });
});

function chart(): KpiChartViewModel {
  return {
    id: 'orders',
    title: 'Order volume',
    description: 'Orders by bucket.',
    format: 'number',
    buckets: [
      { key: '1', label: 'Jul 1', accessibleLabel: 'July 1, 2026' },
      { key: '2', label: 'Jul 2', accessibleLabel: 'July 2, 2026' },
      { key: '3', label: 'Jul 3', accessibleLabel: 'July 3, 2026' },
    ],
    series: [
      { key: 'orders', label: 'Orders', color: '#2563eb', values: [2, 4, 3] },
      { key: 'cancelled', label: 'Cancelled', color: '#dc2626', values: [0, null, 1] },
    ],
  };
}
