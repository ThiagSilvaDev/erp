import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, finalize, of, switchMap } from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzRadioModule } from 'ng-zorro-antd/radio';
import { NzSelectModule } from 'ng-zorro-antd/select';

import { CreateSalesOrderRequestFulfillmentType } from '@generated-api/modules/sales/dtos';
import { CompaniesService } from '@generated-api/modules/companies/Companies.service';
import type {
  AddressResponse,
  CompanyAddressResponse,
} from '@generated-api/modules/companies/dtos';
import { CustomersService } from '@generated-api/modules/customers/Customers.service';
import type { CustomerResponse } from '@generated-api/modules/customers/dtos';
import { CepAddressService } from '../../data-access/cep-address.service';
import { EntityOptionsFacade } from '../../data-access/entity-options.facade';
import { EntityRegistryService } from '../../data-access/entity-registry.service';
import {
  EntityConfig,
  EntityField,
  EntityForm,
  EntityFormValue,
  EntityKey,
  EntityOptionsKey,
  EntitySalesOrderItemFormValue,
  EntitySelectOption,
  EntitySelectOptions,
} from '../../models/entity-view-model';
import { text } from '../../utils/entity-utils';

interface EntityFieldSection {
  title: string;
  fields: EntityField[];
}

type SalesOrderItemForm = FormGroup<{
  productId: FormControl<string>;
  warehouseId: FormControl<string>;
  quantity: FormControl<number>;
}>;

@Component({
  selector: 'app-entity-create-page',
  imports: [
    ReactiveFormsModule,
    NzAlertModule,
    NzButtonModule,
    NzFormModule,
    NzInputModule,
    NzRadioModule,
    NzSelectModule,
  ],
  templateUrl: './entity-create-page.html',
  styleUrl: './entity-create-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EntityCreatePage {
  private readonly cepAddress = inject(CepAddressService);
  private readonly companies = inject(CompaniesService);
  private readonly customers = inject(CustomersService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly entityRegistry = inject(EntityRegistryService);
  private readonly messages = inject(NzMessageService);
  private readonly optionsFacade = inject(EntityOptionsFacade);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly entityKey = signal<EntityKey>(this.entityKeyFromRouteData(this.route.snapshot.data));
  readonly errorMessage = signal<string | null>(null);
  readonly isAddressLoading = signal(false);
  readonly isSaving = signal(false);
  readonly selectOptions = signal<EntitySelectOptions>({});
  readonly salesOrderItems = signal<FormArray<SalesOrderItemForm>>(this.buildSalesOrderItems());

  readonly config = signal<EntityConfig>(this.entityRegistry.getConfig(this.entityKey()));
  readonly createForm = signal<EntityForm>(this.buildForm([]));
  readonly fieldSections = computed<EntityFieldSection[]>(() => {
    const sections = new Map<string, EntityField[]>();
    for (const field of this.config().createFields) {
      if (this.isSalesOrderItemField(field)) {
        continue;
      }
      const title = field.section ?? 'Details';
      sections.set(title, [...(sections.get(title) ?? []), field]);
    }
    return Array.from(sections.entries()).map(([title, fields]) => ({ title, fields }));
  });

  constructor() {
    this.route.data.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((data) => {
      const key = this.entityKeyFromRouteData(data);
      this.entityKey.set(key);
      this.config.set(this.entityRegistry.getConfig(key));
      this.createForm.set(this.buildForm(this.config().createFields));
      this.salesOrderItems.set(this.buildSalesOrderItems());
      this.watchSalesOrderCustomerAddress();
      if (this.config().readonly || this.config().createMode !== 'page') {
        void this.router.navigate(['../'], { relativeTo: this.route });
        return;
      }

      this.loadSelectOptions();
    });
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

  isSalesOrdersCreate(): boolean {
    return this.entityKey() === 'sales-orders';
  }

  productOptions(): EntitySelectOption[] {
    return this.selectOptions().products ?? [];
  }

  warehouseOptions(): EntitySelectOption[] {
    return this.selectOptions().warehouses ?? [];
  }

  addSalesOrderItem(): void {
    this.salesOrderItems().push(this.buildSalesOrderItem());
  }

  removeSalesOrderItem(index: number): void {
    if (this.salesOrderItems().length <= 1) {
      return;
    }
    this.salesOrderItems().removeAt(index);
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

  submitCreate(): void {
    const form = this.createForm();
    const create = this.config().create;
    if (!create) {
      return;
    }

    const isSalesOrdersCreate = this.isSalesOrdersCreate();
    if (isSalesOrdersCreate) {
      this.validateSalesOrderDeliveryRoute();
    }

    if (form.invalid || (isSalesOrdersCreate && this.salesOrderItems().invalid)) {
      form.markAllAsTouched();
      if (isSalesOrdersCreate) {
        this.salesOrderItems().markAllAsTouched();
      }
      return;
    }

    this.isSaving.set(true);
    this.errorMessage.set(null);
    create({
      ...form.getRawValue(),
      ...(isSalesOrdersCreate ? { items: this.salesOrderItemValues() } : {}),
    })
      .pipe(
        finalize(() => this.isSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.messages.success(`${this.config().title} record created.`);
          void this.router.navigate(['../'], { relativeTo: this.route });
        },
        error: () =>
          this.errorMessage.set(`Unable to create ${this.config().title.toLowerCase()} record.`),
      });
  }

  cancel(): void {
    void this.router.navigate(['../'], { relativeTo: this.route });
  }

  private buildForm(fields: EntityField[]): EntityForm {
    const controls: Record<string, FormControl<EntityFormValue>> = {};
    for (const field of fields) {
      if (this.isSalesOrderItemField(field)) {
        continue;
      }
      controls[field.key] = new FormControl<EntityFormValue>('', {
        nonNullable: true,
        validators: [
          ...(field.required ? [Validators.required] : []),
          ...(field.maxLength ? [Validators.maxLength(field.maxLength)] : []),
        ],
      });
    }
    return new FormGroup(controls);
  }

  private buildSalesOrderItems(): FormArray<SalesOrderItemForm> {
    return new FormArray<SalesOrderItemForm>([this.buildSalesOrderItem()]);
  }

  private buildSalesOrderItem(): SalesOrderItemForm {
    return new FormGroup({
      productId: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required],
      }),
      warehouseId: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required],
      }),
      quantity: new FormControl(1, {
        nonNullable: true,
        validators: [Validators.required, Validators.min(1)],
      }),
    });
  }

  private salesOrderItemValues(): EntitySalesOrderItemFormValue[] {
    return this.salesOrderItems().controls.map((control) => control.getRawValue());
  }

  private watchSalesOrderCustomerAddress(): void {
    if (!this.isSalesOrdersCreate()) {
      return;
    }

    const customerIdControl = this.createForm().controls['customerId'];
    if (!customerIdControl) {
      return;
    }

    customerIdControl.valueChanges
      .pipe(
        switchMap((customerId) => this.salesOrderCustomerAddress(text(customerId))),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((address) => this.applyDeliveryAddress(address));
  }

  private salesOrderCustomerAddress(customerId: string) {
    if (!customerId) {
      return of<CompanyAddressResponse | null>(null);
    }

    return this.customers.getCustomerById(customerId).pipe(
      switchMap((customer) => {
        const ownerCompanyId = text(customer.ownerCompanyId);
        if (ownerCompanyId) {
          return this.companies.getCompanyAddress(ownerCompanyId);
        }

        return of(this.companyAddressFromCustomer(customer));
      }),
      catchError(() => {
        this.messages.warning('Unable to load the customer delivery address.');
        return of<CompanyAddressResponse | null>(null);
      }),
    );
  }

  private companyAddressFromCustomer(customer: CustomerResponse): CompanyAddressResponse {
    return {
      companyId: text(customer.ownerCompanyId),
      companyName: customer.name,
      address: customer.address as AddressResponse | undefined,
      deliveryRouteDestinationDescription: this.formatAddress(customer.address),
    };
  }

  private applyDeliveryAddress(address: CompanyAddressResponse | null): void {
    const form = this.createForm();
    form.patchValue({
      deliveryRouteName: address?.companyName ? `${address.companyName} delivery` : '',
      deliveryRouteDestinationDescription:
        address?.deliveryRouteDestinationDescription || this.formatAddress(address?.address),
    });
  }

  private formatAddress(address: AddressResponse | undefined): string {
    if (!address) {
      return '';
    }

    return [
      [address.street, address.number].filter(Boolean).join(', '),
      address.complement,
      address.district,
      [address.city, address.region].filter(Boolean).join(' - '),
      address.postalCode,
      address.country,
    ]
      .filter(Boolean)
      .join(', ');
  }

  private validateSalesOrderDeliveryRoute(): void {
    const form = this.createForm();
    const isDelivery =
      form.controls['fulfillmentType']?.value === CreateSalesOrderRequestFulfillmentType.DELIVERY;

    for (const key of ['deliveryRouteName', 'deliveryRouteDestinationDescription']) {
      const control = form.controls[key];
      if (!control) {
        continue;
      }

      if (isDelivery && !text(control.value).trim()) {
        control.setErrors({ ...control.errors, required: true });
      } else if (control.errors?.['required']) {
        const errors = { ...control.errors };
        delete errors['required'];
        control.setErrors(Object.keys(errors).length ? errors : null);
      }
    }
  }

  private loadSelectOptions(): void {
    this.optionsFacade
      .loadOptions(this.optionKeysFor(this.config().createFields))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((options) => this.selectOptions.set(options));
  }

  private isSalesOrderItemField(field: EntityField): boolean {
    return (
      this.isSalesOrdersCreate() &&
      (field.key === 'productId' || field.key === 'warehouseId' || field.key === 'quantity')
    );
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
