import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzTableModule } from 'ng-zorro-antd/table';

import { EntityColumn, EntityRecord } from '../../models/entity-view-model';

@Component({
  selector: 'app-entity-data-table',
  imports: [NzButtonModule, NzTableModule],
  templateUrl: './entity-data-table.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EntityDataTable {
  readonly columns = input.required<EntityColumn[]>();
  readonly isLoading = input(false);
  readonly rows = input.required<EntityRecord[]>();
  readonly viewRecord = output<EntityRecord>();

  valueFor(record: EntityRecord, key: string): string {
    const value = key.split('.').reduce<unknown>((current, part) => {
      if (current && typeof current === 'object' && part in current) {
        return (current as Record<string, unknown>)[part];
      }
      return undefined;
    }, record);

    if (typeof value === 'string' || typeof value === 'number') {
      return String(value);
    }

    return '-';
  }
}
