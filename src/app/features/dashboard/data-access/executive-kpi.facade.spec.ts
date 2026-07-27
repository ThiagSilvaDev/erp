import type { ExecutiveKpiResponse } from '@generated-api/modules/kpis/dtos';
import { normalizeExecutiveKpis } from './executive-kpi.facade';

describe('normalizeExecutiveKpis', () => {
  it('maps comparisons, snapshots, and nullable metrics into display cards', () => {
    const dashboard = normalizeExecutiveKpis(response());
    const sales = dashboard.sections.find((section) => section.id === 'sales');
    const logistics = dashboard.sections.find((section) => section.id === 'logistics');

    expect(dashboard.period).toEqual(
      expect.objectContaining({
        from: '2026-07-01',
        to: '2026-07-31',
        bucket: 'WEEK',
        timezone: 'UTC',
      }),
    );
    expect(sales?.cards).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ id: 'sales-orders', value: '35', change: 25 }),
        expect.objectContaining({
          id: 'sales-revenue-BRL',
          value: 'R$ 12.500,00',
          change: 25,
        }),
      ]),
    );
    expect(logistics?.cards).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ id: 'logistics-transit-now', snapshot: true, value: '6' }),
        expect.objectContaining({ id: 'logistics-average-hours', change: null }),
      ]),
    );
  });

  it('builds one monetary chart per currency and fills absent bucket values with zero', () => {
    const fixture = response();
    fixture.sales?.cards?.confirmedRevenue?.push({
      currencyCode: 'USD',
      current: 300,
      previous: 0,
      percentageChange: null as never,
    });

    const dashboard = normalizeExecutiveKpis(fixture);
    const sales = dashboard.sections.find((section) => section.id === 'sales');
    const usdChart = sales?.charts.find((chart) => chart.id === 'sales-revenue-USD');

    expect(sales?.charts.map((chart) => chart.id)).toEqual([
      'sales-volume',
      'sales-revenue-BRL',
      'sales-revenue-USD',
    ]);
    expect(usdChart?.series[0]?.values).toEqual([0, 0]);
  });

  it('preserves null trend averages and supplies zeroes for missing counts', () => {
    const fixture = response();
    if (fixture.logistics?.trend?.[0]) {
      fixture.logistics.trend[0].averageDeliveryHours = null as never;
      fixture.logistics.trend[0].created = undefined;
    }

    const dashboard = normalizeExecutiveKpis(fixture);
    const logistics = dashboard.sections.find((section) => section.id === 'logistics');
    const outcomes = logistics?.charts.find((chart) => chart.id === 'logistics-outcomes');
    const hours = logistics?.charts.find((chart) => chart.id === 'logistics-hours');

    expect(outcomes?.series[0]?.values[0]).toBe(0);
    expect(hours?.series[0]?.values[0]).toBeNull();
  });
});

function response(): ExecutiveKpiResponse {
  return {
    generatedAt: '2026-07-27T15:30:00Z',
    period: {
      from: '2026-07-01',
      to: '2026-07-31',
      previousFrom: '2026-05-31',
      previousTo: '2026-06-30',
      bucket: 'WEEK',
      timezone: 'UTC',
    },
    sales: {
      cards: {
        orders: { current: 35, previous: 28, percentageChange: 25 },
        confirmedOrders: { current: 30, previous: 24, percentageChange: 25 },
        cancelledOrders: { current: 5, previous: 4, percentageChange: 25 },
        cancellationRate: {
          currentPercentage: 14.29,
          previousPercentage: 14.29,
          percentagePointChange: 0,
        },
        confirmedRevenue: [
          { currencyCode: 'BRL', current: 12_500, previous: 10_000, percentageChange: 25 },
        ],
        averageOrderValue: [
          { currencyCode: 'BRL', current: 416.67, previous: 416.67, percentageChange: 0 },
        ],
      },
      trend: [
        {
          from: '2026-07-01',
          to: '2026-07-05',
          orders: 8,
          confirmedOrders: 7,
          cancelledOrders: 1,
          confirmedRevenue: [{ currencyCode: 'BRL', amount: 2_800 }],
        },
        {
          from: '2026-07-06',
          to: '2026-07-12',
          orders: 9,
          confirmedOrders: 8,
          cancelledOrders: 1,
          confirmedRevenue: [{ currencyCode: 'BRL', amount: 3_000 }],
        },
      ],
    },
    financial: {
      cards: {
        invoices: { current: 30, previous: 24, percentageChange: 25 },
        invoiced: [
          { currencyCode: 'BRL', current: 12_500, previous: 10_000, percentageChange: 25 },
        ],
        collected: [
          { currencyCode: 'BRL', current: 9_000, previous: 8_000, percentageChange: 12.5 },
        ],
        openReceivables: [{ currencyCode: 'BRL', amount: 3_500 }],
        overdueReceivables: [{ currencyCode: 'BRL', amount: 500 }],
      },
      trend: [
        {
          from: '2026-07-01',
          to: '2026-07-05',
          invoices: 7,
          invoiced: [{ currencyCode: 'BRL', amount: 2_800 }],
          collected: [{ currencyCode: 'BRL', amount: 2_100 }],
        },
      ],
    },
    inventory: {
      current: {
        itemCount: 120,
        quantityOnHand: 1_500,
        reservedQuantity: 200,
        availableQuantity: 1_300,
        outOfStockItems: 8,
      },
      movements: {
        inbound: { current: 300, previous: 250, percentageChange: 20 },
        outbound: { current: 220, previous: 200, percentageChange: 10 },
        reserved: { current: 180, previous: 150, percentageChange: 20 },
        released: { current: 40, previous: 30, percentageChange: 33.33 },
        adjusted: { current: 15, previous: 10, percentageChange: 50 },
      },
      trend: [
        {
          from: '2026-07-01',
          to: '2026-07-05',
          inbound: 70,
          outbound: 45,
          reserved: 35,
          released: 8,
          adjusted: 2,
        },
      ],
    },
    logistics: {
      current: { created: 4, dispatchAssigned: 3, loaded: 2, inTransit: 6 },
      cards: {
        created: { current: 20, previous: 16, percentageChange: 25 },
        delivered: { current: 15, previous: 12, percentageChange: 25 },
        cancelled: { current: 2, previous: 1, percentageChange: 100 },
        deliveryRate: {
          currentPercentage: 88.24,
          previousPercentage: 92.31,
          percentagePointChange: -4.07,
        },
        averageDeliveryHours: {
          current: 7.5,
          previous: 8.25,
          percentageChange: null as never,
        },
      },
      trend: [
        {
          from: '2026-07-01',
          to: '2026-07-05',
          created: 5,
          delivered: 4,
          cancelled: 1,
          averageDeliveryHours: 7.25,
        },
      ],
    },
  };
}
