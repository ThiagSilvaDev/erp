import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzIconModule } from 'ng-zorro-antd/icon';

import { DashboardSummaryCard } from '../../models/dashboard-view-model';

@Component({
  selector: 'app-dashboard-summary-cards',
  imports: [RouterLink, NzCardModule, NzIconModule],
  templateUrl: './dashboard-summary-cards.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardSummaryCards {
  readonly cards = input.required<DashboardSummaryCard[]>();
}
