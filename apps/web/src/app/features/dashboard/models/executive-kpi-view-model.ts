export type KpiBucket = 'DAY' | 'WEEK' | 'MONTH';
export type KpiChartFormat = 'number' | 'hours' | 'currency';

export interface KpiFilterModel {
  from: string;
  to: string;
  bucket: KpiBucket;
}

export interface KpiPeriodViewModel {
  from: string;
  to: string;
  previousFrom: string;
  previousTo: string;
  bucket: KpiBucket;
  timezone: string;
  label: string;
  comparisonLabel: string;
}

export interface KpiMetricCardViewModel {
  id: string;
  label: string;
  value: string;
  previous?: string;
  change?: number | null;
  changeSuffix?: '%' | ' pp';
  snapshot?: boolean;
}

export interface KpiChartBucketViewModel {
  key: string;
  label: string;
  accessibleLabel: string;
}

export interface KpiChartSeriesViewModel {
  key: string;
  label: string;
  color: string;
  values: (number | null)[];
}

export interface KpiChartViewModel {
  id: string;
  title: string;
  description: string;
  format: KpiChartFormat;
  currencyCode?: string;
  buckets: KpiChartBucketViewModel[];
  series: KpiChartSeriesViewModel[];
}

export interface KpiSectionViewModel {
  id: 'sales' | 'financial' | 'inventory' | 'logistics';
  title: string;
  description: string;
  cards: KpiMetricCardViewModel[];
  charts: KpiChartViewModel[];
}

export interface ExecutiveKpiViewModel {
  generatedAt: string;
  generatedAtLabel: string;
  period: KpiPeriodViewModel;
  sections: KpiSectionViewModel[];
}

export type KpiLoadErrorKind = 'forbidden' | 'validation' | 'generic';

export interface KpiLoadError {
  kind: KpiLoadErrorKind;
  message: string;
  details: string[];
}
