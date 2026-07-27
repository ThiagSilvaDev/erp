import { Injectable, inject } from '@angular/core';
import { map } from 'rxjs';

import { KpisService } from '@generated-api/modules/kpis/Kpis.service';
import type {
  CountComparison,
  DecimalComparison,
  ExecutiveKpiResponse,
  FinancialTrend,
  GetExecutiveKpisParams,
  InventoryTrend,
  LogisticsTrend,
  MoneyComparison,
  MoneyValue,
  RateComparison,
  SalesTrend,
} from '@generated-api/modules/kpis/dtos';
import {
  ExecutiveKpiViewModel,
  KpiBucket,
  KpiChartBucketViewModel,
  KpiChartViewModel,
  KpiMetricCardViewModel,
  KpiSectionViewModel,
} from '../models/executive-kpi-view-model';
import {
  formatBucketLabel,
  formatDateRange,
  formatHours,
  formatMoney,
  formatNumber,
  formatPercent,
  formatTimestamp,
} from '../utils/kpi-formatters';

const COLORS = ['#2563eb', '#16a34a', '#dc2626', '#9333ea', '#ea580c'];

@Injectable({ providedIn: 'root' })
export class ExecutiveKpiFacade {
  private readonly api = inject(KpisService);

  load(params?: GetExecutiveKpisParams) {
    return this.api.getExecutiveKpis(params).pipe(map(normalizeExecutiveKpis));
  }
}

export function normalizeExecutiveKpis(response: ExecutiveKpiResponse): ExecutiveKpiViewModel {
  const period = response.period;
  const bucket = isBucket(period?.bucket) ? period.bucket : 'DAY';

  return {
    generatedAt: response.generatedAt ?? '',
    generatedAtLabel: formatTimestamp(response.generatedAt),
    period: {
      from: period?.from ?? '',
      to: period?.to ?? '',
      previousFrom: period?.previousFrom ?? '',
      previousTo: period?.previousTo ?? '',
      bucket,
      timezone: period?.timezone ?? 'UTC',
      label: formatDateRange(period?.from, period?.to),
      comparisonLabel: formatDateRange(period?.previousFrom, period?.previousTo),
    },
    sections: [
      salesSection(response),
      financialSection(response),
      inventorySection(response),
      logisticsSection(response),
    ],
  };
}

function salesSection(response: ExecutiveKpiResponse): KpiSectionViewModel {
  const sales = response.sales;
  const cards = sales?.cards;
  const trend = sales?.trend ?? [];
  const revenueCurrencies = currencies(
    cards?.confirmedRevenue,
    trend.flatMap((item) => item.confirmedRevenue ?? []),
  );
  const orderBuckets = buckets(trend);

  return {
    id: 'sales',
    title: 'Sales',
    description: 'Order volume, conversion quality, and confirmed revenue.',
    cards: [
      countCard('sales-orders', 'Orders', cards?.orders),
      countCard('sales-confirmed', 'Confirmed orders', cards?.confirmedOrders),
      countCard('sales-cancelled', 'Cancelled orders', cards?.cancelledOrders),
      rateCard('sales-cancellation-rate', 'Cancellation rate', cards?.cancellationRate),
      ...moneyComparisonCards('sales-revenue', 'Confirmed revenue', cards?.confirmedRevenue),
      ...moneyComparisonCards('sales-aov', 'Average order value', cards?.averageOrderValue),
    ],
    charts: [
      {
        id: 'sales-volume',
        title: 'Order volume',
        description: 'Orders by status for each requested bucket.',
        format: 'number',
        buckets: orderBuckets,
        series: [
          series(
            'orders',
            'Orders',
            COLORS[0],
            trend.map((item) => count(item.orders)),
          ),
          series(
            'confirmed-orders',
            'Confirmed',
            COLORS[1],
            trend.map((item) => count(item.confirmedOrders)),
          ),
          series(
            'cancelled-orders',
            'Cancelled',
            COLORS[2],
            trend.map((item) => count(item.cancelledOrders)),
          ),
        ],
      },
      ...revenueCurrencies.map(
        (currencyCode): KpiChartViewModel => ({
          id: `sales-revenue-${currencyCode}`,
          title: `Confirmed revenue · ${currencyCode}`,
          description: 'Confirmed sales revenue for each requested bucket.',
          format: 'currency',
          currencyCode,
          buckets: orderBuckets,
          series: [
            series(
              `revenue-${currencyCode}`,
              'Confirmed revenue',
              COLORS[0],
              trend.map((item) => money(item.confirmedRevenue, currencyCode)),
            ),
          ],
        }),
      ),
    ],
  };
}

function financialSection(response: ExecutiveKpiResponse): KpiSectionViewModel {
  const financial = response.financial;
  const cards = financial?.cards;
  const trend = financial?.trend ?? [];
  const financialCurrencies = currencies(
    cards?.invoiced,
    cards?.collected,
    cards?.openReceivables,
    cards?.overdueReceivables,
    trend.flatMap((item) => item.invoiced ?? []),
    trend.flatMap((item) => item.collected ?? []),
  );
  const financialBuckets = buckets(trend);

  return {
    id: 'financial',
    title: 'Financial',
    description: 'Billing, collections, and the current receivables position.',
    cards: [
      countCard('financial-invoices', 'Invoices', cards?.invoices),
      ...moneyComparisonCards('financial-invoiced', 'Invoiced', cards?.invoiced),
      ...moneyComparisonCards('financial-collected', 'Collected', cards?.collected),
      ...moneySnapshotCards('financial-open', 'Open receivables', cards?.openReceivables),
      ...moneySnapshotCards('financial-overdue', 'Overdue receivables', cards?.overdueReceivables),
    ],
    charts: [
      {
        id: 'financial-invoices',
        title: 'Invoices',
        description: 'Invoices issued for each requested bucket.',
        format: 'number',
        buckets: financialBuckets,
        series: [
          series(
            'invoices',
            'Invoices',
            COLORS[0],
            trend.map((item) => count(item.invoices)),
          ),
        ],
      },
      ...financialCurrencies.map(
        (currencyCode): KpiChartViewModel => ({
          id: `financial-values-${currencyCode}`,
          title: `Invoiced and collected · ${currencyCode}`,
          description: 'Amounts invoiced and collected for each requested bucket.',
          format: 'currency',
          currencyCode,
          buckets: financialBuckets,
          series: [
            series(
              `invoiced-${currencyCode}`,
              'Invoiced',
              COLORS[0],
              trend.map((item) => money(item.invoiced, currencyCode)),
            ),
            series(
              `collected-${currencyCode}`,
              'Collected',
              COLORS[1],
              trend.map((item) => money(item.collected, currencyCode)),
            ),
          ],
        }),
      ),
    ],
  };
}

function inventorySection(response: ExecutiveKpiResponse): KpiSectionViewModel {
  const inventory = response.inventory;
  const current = inventory?.current;
  const movements = inventory?.movements;
  const trend = inventory?.trend ?? [];

  return {
    id: 'inventory',
    title: 'Inventory',
    description: 'Live stock availability and period movement activity.',
    cards: [
      snapshotCard('inventory-items', 'Items', current?.itemCount),
      snapshotCard('inventory-on-hand', 'Quantity on hand', current?.quantityOnHand),
      snapshotCard('inventory-reserved-now', 'Reserved quantity', current?.reservedQuantity),
      snapshotCard('inventory-available', 'Available quantity', current?.availableQuantity),
      snapshotCard('inventory-out-of-stock', 'Out-of-stock items', current?.outOfStockItems),
      countCard('inventory-inbound', 'Inbound', movements?.inbound),
      countCard('inventory-outbound', 'Outbound', movements?.outbound),
      countCard('inventory-reserved', 'Reserved', movements?.reserved),
      countCard('inventory-released', 'Released', movements?.released),
      countCard('inventory-adjusted', 'Adjusted', movements?.adjusted),
    ],
    charts: [
      {
        id: 'inventory-movements',
        title: 'Stock movements',
        description: 'Inventory movement quantities for each requested bucket.',
        format: 'number',
        buckets: buckets(trend),
        series: [
          series(
            'inbound',
            'Inbound',
            COLORS[1],
            trend.map((item) => count(item.inbound)),
          ),
          series(
            'outbound',
            'Outbound',
            COLORS[0],
            trend.map((item) => count(item.outbound)),
          ),
          series(
            'reserved',
            'Reserved',
            COLORS[3],
            trend.map((item) => count(item.reserved)),
          ),
          series(
            'released',
            'Released',
            COLORS[4],
            trend.map((item) => count(item.released)),
          ),
          series(
            'adjusted',
            'Adjusted',
            COLORS[2],
            trend.map((item) => count(item.adjusted)),
          ),
        ],
      },
    ],
  };
}

function logisticsSection(response: ExecutiveKpiResponse): KpiSectionViewModel {
  const logistics = response.logistics;
  const current = logistics?.current;
  const cards = logistics?.cards;
  const trend = logistics?.trend ?? [];
  const logisticsBuckets = buckets(trend);

  return {
    id: 'logistics',
    title: 'Logistics',
    description: 'Live shipment workload and delivery performance.',
    cards: [
      snapshotCard('logistics-created-now', 'Created now', current?.created),
      snapshotCard('logistics-assigned-now', 'Dispatch assigned', current?.dispatchAssigned),
      snapshotCard('logistics-loaded-now', 'Loaded', current?.loaded),
      snapshotCard('logistics-transit-now', 'In transit', current?.inTransit),
      countCard('logistics-created', 'Created', cards?.created),
      countCard('logistics-delivered', 'Delivered', cards?.delivered),
      countCard('logistics-cancelled', 'Cancelled', cards?.cancelled),
      rateCard('logistics-delivery-rate', 'Delivery rate', cards?.deliveryRate),
      decimalCard('logistics-average-hours', 'Average delivery time', cards?.averageDeliveryHours),
    ],
    charts: [
      {
        id: 'logistics-outcomes',
        title: 'Shipment outcomes',
        description: 'Created, delivered, and cancelled shipments by bucket.',
        format: 'number',
        buckets: logisticsBuckets,
        series: [
          series(
            'created',
            'Created',
            COLORS[0],
            trend.map((item) => count(item.created)),
          ),
          series(
            'delivered',
            'Delivered',
            COLORS[1],
            trend.map((item) => count(item.delivered)),
          ),
          series(
            'cancelled',
            'Cancelled',
            COLORS[2],
            trend.map((item) => count(item.cancelled)),
          ),
        ],
      },
      {
        id: 'logistics-hours',
        title: 'Average delivery time',
        description: 'Average hours from dispatch to delivery by bucket.',
        format: 'hours',
        buckets: logisticsBuckets,
        series: [
          series(
            'average-delivery-hours',
            'Average delivery time',
            COLORS[3],
            trend.map((item) => nullable(item.averageDeliveryHours)),
          ),
        ],
      },
    ],
  };
}

function countCard(
  id: string,
  label: string,
  comparison: CountComparison | undefined,
): KpiMetricCardViewModel {
  return {
    id,
    label,
    value: formatNumber(comparison?.current),
    previous: `Previous ${formatNumber(comparison?.previous)}`,
    change: nullable(comparison?.percentageChange),
    changeSuffix: '%',
  };
}

function rateCard(
  id: string,
  label: string,
  comparison: RateComparison | undefined,
): KpiMetricCardViewModel {
  return {
    id,
    label,
    value: formatPercent(comparison?.currentPercentage),
    previous: `Previous ${formatPercent(comparison?.previousPercentage)}`,
    change: nullable(comparison?.percentagePointChange),
    changeSuffix: ' pp',
  };
}

function decimalCard(
  id: string,
  label: string,
  comparison: DecimalComparison | undefined,
): KpiMetricCardViewModel {
  return {
    id,
    label,
    value: formatHours(comparison?.current),
    previous: `Previous ${formatHours(comparison?.previous)}`,
    change: nullable(comparison?.percentageChange),
    changeSuffix: '%',
  };
}

function snapshotCard(
  id: string,
  label: string,
  value: number | undefined,
): KpiMetricCardViewModel {
  return { id, label, value: formatNumber(value), snapshot: true };
}

function moneyComparisonCards(
  id: string,
  label: string,
  comparisons: MoneyComparison[] | undefined,
): KpiMetricCardViewModel[] {
  return (comparisons ?? []).filter(hasCurrency).map((comparison) => ({
    id: `${id}-${comparison.currencyCode}`,
    label: `${label} · ${comparison.currencyCode}`,
    value: formatMoney(comparison.current, comparison.currencyCode),
    previous: `Previous ${formatMoney(comparison.previous, comparison.currencyCode)}`,
    change: nullable(comparison.percentageChange),
    changeSuffix: '%',
  }));
}

function moneySnapshotCards(
  id: string,
  label: string,
  values: MoneyValue[] | undefined,
): KpiMetricCardViewModel[] {
  return (values ?? []).filter(hasCurrency).map((value) => ({
    id: `${id}-${value.currencyCode}`,
    label: `${label} · ${value.currencyCode}`,
    value: formatMoney(value.amount, value.currencyCode),
    snapshot: true,
  }));
}

function buckets(
  trend: (SalesTrend | FinancialTrend | InventoryTrend | LogisticsTrend)[],
): KpiChartBucketViewModel[] {
  return trend.map((item, index) => ({
    key: `${item.from ?? index}-${item.to ?? index}`,
    label: formatBucketLabel(item.from, item.to),
    accessibleLabel: formatDateRange(item.from, item.to),
  }));
}

function series(key: string, label: string, color: string | undefined, values: (number | null)[]) {
  return { key, label, color: color ?? '#2563eb', values };
}

function currencies(...groups: ({ currencyCode?: string }[] | undefined)[]): string[] {
  return [
    ...new Set(
      groups.flatMap((group) =>
        (group ?? [])
          .map((item) => item.currencyCode)
          .filter((value): value is string => Boolean(value)),
      ),
    ),
  ].sort();
}

function money(values: MoneyValue[] | undefined, currencyCode: string): number {
  return count(values?.find((value) => value.currencyCode === currencyCode)?.amount);
}

function count(value: number | null | undefined): number {
  return value ?? 0;
}

function nullable(value: number | null | undefined): number | null {
  return value ?? null;
}

function hasCurrency<T extends { currencyCode?: string }>(
  value: T,
): value is T & { currencyCode: string } {
  return Boolean(value.currencyCode);
}

function isBucket(value: unknown): value is KpiBucket {
  return value === 'DAY' || value === 'WEEK' || value === 'MONTH';
}
