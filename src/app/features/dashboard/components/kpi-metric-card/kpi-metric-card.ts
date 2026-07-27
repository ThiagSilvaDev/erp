import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { NzCardModule } from 'ng-zorro-antd/card';

import { KpiMetricCardViewModel } from '../../models/executive-kpi-view-model';

@Component({
  selector: 'app-kpi-metric-card',
  imports: [NzCardModule],
  templateUrl: './kpi-metric-card.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KpiMetricCard {
  readonly card = input.required<KpiMetricCardViewModel>();

  readonly changeText = computed(() => {
    const card = this.card();
    if (card.change === undefined) {
      return '';
    }
    if (card.change === null) {
      return 'No previous baseline';
    }

    const sign = card.change > 0 ? '+' : '';
    return `${sign}${card.change.toLocaleString('pt-BR', { maximumFractionDigits: 2 })}${card.changeSuffix ?? '%'}`;
  });

  readonly changeClass = computed(() => {
    const change = this.card().change;
    if (change == null || change === 0) {
      return 'text-slate-500';
    }
    return change > 0 ? 'text-emerald-700' : 'text-rose-700';
  });
}
