import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzDropdownMenuComponent, NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzMenuModule } from 'ng-zorro-antd/menu';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzTableModule } from 'ng-zorro-antd/table';

import { EntityAction, EntityColumn, EntityRecord } from '../../models/entity-view-model';

@Component({
  selector: 'app-entity-data-table',
  imports: [
    NzButtonModule,
    NzDropDownModule,
    NzIconModule,
    NzMenuModule,
    NzTableModule,
    NzTagModule,
    NzDropdownMenuComponent,
  ],
  templateUrl: './entity-data-table.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EntityDataTable {
  readonly actions = input<EntityAction[]>([]);
  readonly activeAction = input<string | null>(null);
  readonly columns = input.required<EntityColumn[]>();
  readonly isLoading = input(false);
  readonly rows = input.required<EntityRecord[]>();
  readonly runRecordAction = output<{ action: EntityAction; record: EntityRecord }>();
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

  visibleActions(record: EntityRecord): EntityAction[] {
    return this.actions().filter((action) => !action.isVisible || action.isVisible(record));
  }

  inlineActions(record: EntityRecord): EntityAction[] {
    return this.visibleActions(record).filter((action) => action.icon);
  }

  menuActions(record: EntityRecord): EntityAction[] {
    return this.visibleActions(record).filter((action) => !action.icon);
  }

  actionKey(action: EntityAction, record: EntityRecord): string {
    return `${action.label}:${this.valueFor(record, 'id')}`;
  }

  isTagColumn(key: string): boolean {
    return key === 'direction' || key === 'status' || key === 'role' || key === 'type';
  }

  tagColor(value: string): string {
    switch (value) {
      case 'ACTIVE':
      case 'CONFIRMED':
      case 'PAID':
      case 'POSTED':
      case 'SETTLED':
      case 'RECEIVABLE':
      case 'CREDIT':
      case 'REVENUE':
        return 'green';
      case 'DRAFT':
      case 'OPEN':
      case 'MANAGER':
        return 'blue';
      case 'ADMIN':
      case 'FACTORY':
      case 'CORRESPONDENT':
      case 'ASSET':
      case 'EQUITY':
        return 'purple';
      case 'CANCELLED':
      case 'INACTIVE':
      case 'PAYABLE':
      case 'REVERSED':
      case 'DEBIT':
      case 'EXPENSE':
      case 'LIABILITY':
        return 'red';
      default:
        return 'default';
    }
  }
}
