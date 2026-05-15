export interface DashboardSummaryCard {
  label: string;
  value: number;
  path: string;
  icon: string;
}

export interface DashboardRecentRecord {
  module: string;
  name: string;
  detail: string;
  path: string;
}

export interface DashboardViewModel {
  cards: DashboardSummaryCard[];
  recentRecords: DashboardRecentRecord[];
}
