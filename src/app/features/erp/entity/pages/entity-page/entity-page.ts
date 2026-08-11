import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, finalize, of } from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzRadioModule } from 'ng-zorro-antd/radio';
import { NzSelectModule } from 'ng-zorro-antd/select';

import { EntityDataTable } from '../../components/entity-data-table/entity-data-table';
import { CepAddressService } from '../../data-access/cep-address.service';
import { EntityOptionsFacade } from '../../data-access/entity-options.facade';
import { EntityRegistryService } from '../../data-access/entity-registry.service';
import {
  EntityAction,
  EntityConfig,
  EntityField,
  EntityForm,
  EntityFormValue,
  EntityKey,
  EntityOptionsKey,
  EntityRecord,
  EntitySelectOption,
  EntitySelectOptions,
} from '../../models/entity-view-model';
import { getValue, text } from '../../utils/entity-utils';

@Component({
  selector: 'app-entity',
  imports: [
    ReactiveFormsModule,
    NzAlertModule,
    NzButtonModule,
    NzDescriptionsModule,
    NzFormModule,
    NzIconModule,
    NzInputModule,
    NzModalModule,
    NzPaginationModule,
    NzRadioModule,
    NzSelectModule,
    EntityDataTable,
  ],
  templateUrl: './entity-page.html',
  styleUrl: './entity-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EntityPage {
  private readonly cepAddress = inject(CepAddressService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly entityRegistry = inject(EntityRegistryService);
  private readonly messages = inject(NzMessageService);
  private readonly optionsFacade = inject(EntityOptionsFacade);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly entityKey = signal<EntityKey>(this.entityKeyFromRouteData(this.route.snapshot.data));
  readonly pageIndex = signal(1);
  readonly pageSize = signal(10);
  readonly total = signal(0);
  readonly rows = signal<EntityRecord[]>([]);
  readonly selectedRecord = signal<EntityRecord | null>(null);
  readonly isLoading = signal(false);
  readonly isAddressLoading = signal(false);
  readonly isSaving = signal(false);
  readonly isCreateOpen = signal(false);
  readonly isDetailOpen = signal(false);
  readonly activeAction = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly selectOptions = signal<EntitySelectOptions>({});

  readonly config = signal<EntityConfig>(this.entityRegistry.getConfig(this.entityKey()));
  readonly searchForm = signal<EntityForm>(this.buildForm([]));
  readonly createForm = signal<EntityForm>(this.buildForm([]));

  constructor() {
    this.route.data.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((data) => {
      const key = this.entityKeyFromRouteData(data);
      this.entityKey.set(key);
      this.config.set(this.entityRegistry.getConfig(key));
      this.pageIndex.set(1);
      this.searchForm.set(this.buildForm(this.config().searchFields));
      this.createForm.set(this.buildForm(this.config().createFields));
      this.loadRows();
      this.loadSelectOptions();
    });
  }

  valueFor(record: EntityRecord, key: string): string {
    return getValue(record, key);
  }

  optionsFor(field: EntityField): EntitySelectOption[] {
    if (field.options) {
      return field.options;
    }
    return field.optionsKey ? (this.selectOptions()[field.optionsKey] ?? []) : [];
  }

  isPostalCodeField(field: EntityField): boolean {
    return field.key === 'postalCode';
  }

  lookupPostalCode(): void {
    const form = this.createForm();
    const postalCodeControl = form.controls['postalCode'];
    if (!postalCodeControl || this.isAddressLoading()) {
      return;
    }

    const cep = this.cepAddress.normalizeCep(text(postalCodeControl.value));
    if (cep.length !== 8) {
      return;
    }

    this.isAddressLoading.set(true);
    this.cepAddress
      .lookup(cep)
      .pipe(
        finalize(() => this.isAddressLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (address) => {
          form.patchValue({
            postalCode: address.postalCode,
            street: address.street,
            district: address.district,
            city: address.city,
            region: address.region,
            country: address.country,
          });
        },
        error: () => this.messages.warning('Unable to find an address for this CEP.'),
      });
  }

  openCreate(): void {
    if (this.config().readonly) {
      return;
    }
    if (this.config().createMode === 'page') {
      void this.router.navigate(['new'], { relativeTo: this.route });
      return;
    }
    this.createForm().reset();
    this.isCreateOpen.set(true);
  }

  closeCreate(): void {
    this.isCreateOpen.set(false);
  }

  openDetail(record: EntityRecord): void {
    const id = text('id' in record ? record.id : '');
    const detailRoute = this.config().detailRoute;

    if (detailRoute && id) {
      void this.router.navigateByUrl(detailRoute(id));
      return;
    }

    if (!id) {
      this.selectedRecord.set(record);
      this.isDetailOpen.set(true);
      return;
    }

    this.config()
      .getById(id)
      .pipe(
        catchError(() => of(record)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((detail) => {
        this.selectedRecord.set(detail);
        this.isDetailOpen.set(true);
      });
  }

  closeDetail(): void {
    this.isDetailOpen.set(false);
  }

  submitSearch(): void {
    this.pageIndex.set(1);
    this.loadRows();
  }

  resetSearch(): void {
    this.searchForm().reset();
    this.submitSearch();
  }

  submitCreate(): void {
    const form = this.createForm();
    const create = this.config().create;
    if (!create) {
      return;
    }

    if (form.invalid) {
      form.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    create(form.getRawValue())
      .pipe(
        finalize(() => this.isSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (record) => {
          this.messages.success(`${this.config().title} record created.`);
          this.isCreateOpen.set(false);
          this.loadRows();
          this.openDetail(record);
        },
        error: () =>
          this.messages.error(`Unable to create ${this.config().title.toLowerCase()} record.`),
      });
  }

  runAction(action: EntityAction, record: EntityRecord): void {
    const id = text('id' in record ? record.id : action.label);
    this.activeAction.set(`${action.label}:${id}`);
    action
      .run(record)
      .pipe(
        finalize(() => this.activeAction.set(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (updated) => {
          if (action.successMessage !== null) {
            this.messages.success(action.successMessage ?? `${action.label} completed.`);
          }
          if (action.refreshAfterRun !== false) {
            this.loadRows();
          }
          this.selectedRecord.set(updated);
        },
        error: () => this.messages.error(`${action.label} failed.`),
      });
  }

  changePage(page: number): void {
    this.pageIndex.set(page);
    this.loadRows();
  }

  private loadRows(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.config()
      .list(this.compactFormValue(this.searchForm()), this.pageIndex() - 1, this.pageSize())
      .pipe(
        finalize(() => this.isLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (page) => {
          this.rows.set(page.content);
          this.total.set(page.totalElements);
        },
        error: () => {
          this.rows.set([]);
          this.total.set(0);
          this.errorMessage.set(`Unable to load ${this.config().title.toLowerCase()} records.`);
        },
      });
  }

  private loadSelectOptions(): void {
    const fields = [
      ...this.config().searchFields,
      ...(this.config().createMode === 'page' || this.config().readonly
        ? []
        : this.config().createFields),
    ];

    this.optionsFacade
      .loadOptions(this.optionKeysFor(fields))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((options) => this.selectOptions.set(options));
  }

  private buildForm(fields: EntityField[]): EntityForm {
    const controls: Record<string, FormControl<EntityFormValue>> = {};
    for (const field of fields) {
      controls[field.key] = new FormControl<EntityFormValue>(field.defaultValue ?? '', {
        nonNullable: true,
        validators: field.required ? [Validators.required] : [],
      });
    }
    return new FormGroup(controls);
  }

  private compactFormValue(form: EntityForm): Record<string, string> {
    const value: Record<string, string> = {};
    for (const [key, raw] of Object.entries(form.getRawValue())) {
      const normalized = text(raw).trim();
      if (normalized) {
        value[key] = normalized;
      }
    }
    return value;
  }

  private optionKeysFor(fields: EntityField[]): EntityOptionsKey[] {
    return [...new Set(fields.flatMap((field) => (field.optionsKey ? [field.optionsKey] : [])))];
  }

  private entityKeyFromRouteData(data: Record<string, unknown>): EntityKey {
    const entity = data['entity'];

    if (typeof entity !== 'string') {
      throw new Error('Entity route data is missing.');
    }

    return entity as EntityKey;
  }
}
