import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzTableModule } from 'ng-zorro-antd/table';

import { DashboardRecentRecord } from '../../models/dashboard-view-model';

@Component({
  selector: 'app-recent-records-table',
  imports: [RouterLink, NzCardModule, NzTableModule],
  templateUrl: './recent-records-table.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RecentRecordsTable {
  readonly isLoading = input(false);
  readonly records = input.required<DashboardRecentRecord[]>();
}
