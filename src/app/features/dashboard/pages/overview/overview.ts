import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { DashboardSummaryCards } from '../../components/dashboard-summary-cards/dashboard-summary-cards';
import { RecentRecordsTable } from '../../components/recent-records-table/recent-records-table';
import { DashboardOverviewFacade } from '../../data-access/dashboard-overview.facade';
import { DashboardRecentRecord, DashboardSummaryCard } from '../../models/dashboard-view-model';

@Component({
  selector: 'app-overview',
  imports: [DashboardSummaryCards, RecentRecordsTable],
  templateUrl: './overview.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Overview {
  private readonly destroyRef = inject(DestroyRef);
  private readonly facade = inject(DashboardOverviewFacade);

  readonly isLoading = signal(true);
  readonly cards = signal<DashboardSummaryCard[]>([]);
  readonly recentRecords = signal<DashboardRecentRecord[]>([]);

  constructor() {
    this.facade
      .loadOverview()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(({ cards, recentRecords }) => {
        this.cards.set(cards);
        this.recentRecords.set(recentRecords);
        this.isLoading.set(false);
      });
  }
}
