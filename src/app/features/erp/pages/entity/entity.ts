import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { catchError, finalize, map, of } from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzSelectModule } from 'ng-zorro-antd/select';

import { CatalogService } from '../../../../api/modules/catalog/catalog.service';
import { CompaniesService } from '../../../../api/modules/companies/companies.service';
import { ContactPersonsService } from '../../../../api/modules/contact-persons/contact-persons.service';
import { CustomersService } from '../../../../api/modules/customers/customers.service';
import { SuppliersService } from '../../../../api/modules/suppliers/suppliers.service';
import { UsersService } from '../../../../api/modules/users/users.service';
import { EntityDataTable } from '../../components/entity-data-table/entity-data-table';
import { EntityOptionsFacade } from '../../data-access/entity-options.facade';
import {
  EntityConfig,
  EntityField,
  EntityForm,
  EntityFormValue,
  EntityKey,
  EntityRecord,
  EntitySelectOption,
  EntitySelectOptions,
} from '../../models/entity-view-model';

function text(value: unknown): string {
  if (typeof value === 'string' || typeof value === 'number') {
    return String(value);
  }
  return '';
}

function getValue(record: EntityRecord, key: string): string {
  const value = key.split('.').reduce<unknown>((current, part) => {
    if (current && typeof current === 'object' && part in current) {
      return (current as Record<string, unknown>)[part];
    }
    return undefined;
  }, record);

  return text(value) || '-';
}

@Component({
  selector: 'app-entity',
  imports: [
    ReactiveFormsModule,
    NzAlertModule,
    NzButtonModule,
    NzDescriptionsModule,
    NzDrawerModule,
    NzFormModule,
    NzIconModule,
    NzInputModule,
    NzPaginationModule,
    NzSelectModule,
    EntityDataTable,
  ],
  templateUrl: './entity.html',
  styleUrl: './entity.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Entity {
  private readonly catalog = inject(CatalogService);
  private readonly companies = inject(CompaniesService);
  private readonly contactPersons = inject(ContactPersonsService);
  private readonly customers = inject(CustomersService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly messages = inject(NzMessageService);
  private readonly optionsFacade = inject(EntityOptionsFacade);
  private readonly route = inject(ActivatedRoute);
  private readonly suppliers = inject(SuppliersService);
  private readonly users = inject(UsersService);

  readonly entityKey = signal<EntityKey>('companies');
  readonly pageIndex = signal(1);
  readonly pageSize = signal(10);
  readonly total = signal(0);
  readonly rows = signal<EntityRecord[]>([]);
  readonly selectedRecord = signal<EntityRecord | null>(null);
  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly isCreateOpen = signal(false);
  readonly isDetailOpen = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly selectOptions = signal<EntitySelectOptions>({});

  readonly configs = this.createConfigs();
  readonly config = computed(() => this.configs[this.entityKey()]);
  readonly searchForm = signal<EntityForm>(this.buildForm([]));
  readonly createForm = signal<EntityForm>(this.buildForm([]));

  constructor() {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const key = params.get('entity');
      this.entityKey.set(this.isEntityKey(key) ? key : 'companies');
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
    return field.optionsKey ? this.selectOptions()[field.optionsKey] : [];
  }

  openCreate(): void {
    this.createForm().reset();
    this.isCreateOpen.set(true);
  }

  closeCreate(): void {
    this.isCreateOpen.set(false);
  }

  openDetail(record: EntityRecord): void {
    const id = text('id' in record ? record.id : '');
    if (!id) {
      this.selectedRecord.set(record);
      this.isDetailOpen.set(true);
      return;
    }

    this.config()
      .getById(id)
      .pipe(catchError(() => of(record)), takeUntilDestroyed(this.destroyRef))
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
    if (form.invalid) {
      form.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this.config()
      .create(form.getRawValue())
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
        error: () => this.messages.error(`Unable to create ${this.config().title.toLowerCase()} record.`),
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
    this.optionsFacade
      .loadOptions()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((options) => this.selectOptions.set(options));
  }

  private buildForm(fields: EntityField[]): EntityForm {
    const controls: Record<string, FormControl<EntityFormValue>> = {};
    for (const field of fields) {
      controls[field.key] = new FormControl<EntityFormValue>('', {
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

  private isEntityKey(value: string | null): value is EntityKey {
    return Boolean(value && value in this.configs);
  }

  private createConfigs(): Record<EntityKey, EntityConfig> {
    const partySearch: EntityField[] = [
      { key: 'name', label: 'Name' },
      { key: 'document', label: 'Document' },
      { key: 'companyId', label: 'Company', type: 'select', optionsKey: 'companies' },
    ];
    const partyFields: EntityField[] = [
      { key: 'name', label: 'Name', required: true },
      { key: 'document', label: 'Document', required: true },
      { key: 'companyId', label: 'Company', required: true, type: 'select', optionsKey: 'companies' },
      { key: 'email', label: 'Email', type: 'email' },
      { key: 'phone', label: 'Phone' },
      { key: 'street', label: 'Street' },
      { key: 'number', label: 'Number' },
      { key: 'district', label: 'District' },
      { key: 'city', label: 'City' },
      { key: 'region', label: 'Region' },
      { key: 'country', label: 'Country' },
      { key: 'postalCode', label: 'Postal code' },
    ];

    return {
      companies: {
        key: 'companies',
        title: 'Companies',
        description: 'Legal entities and operating companies.',
        columns: [
          { key: 'tradeName', label: 'Trade name' },
          { key: 'legalName', label: 'Legal name' },
          { key: 'cnpj.number', label: 'CNPJ' },
          { key: 'status', label: 'Status' },
        ],
        detailFields: [
          { key: 'legalName', label: 'Legal name' },
          { key: 'tradeName', label: 'Trade name' },
          { key: 'cnpj.number', label: 'CNPJ' },
          { key: 'type', label: 'Type' },
          { key: 'status', label: 'Status' },
          { key: 'contact.email', label: 'Email' },
          { key: 'contact.phone', label: 'Phone' },
          { key: 'address.city', label: 'City' },
          { key: 'address.country', label: 'Country' },
        ],
        searchFields: [
          { key: 'name', label: 'Name' },
          { key: 'cnpj', label: 'CNPJ' },
        ],
        createFields: [
          { key: 'legalName', label: 'Legal name', required: true },
          { key: 'tradeName', label: 'Trade name' },
          { key: 'cnpj', label: 'CNPJ', required: true },
          { key: 'type', label: 'Type', required: true, type: 'select' },
          { key: 'email', label: 'Email', type: 'email' },
          { key: 'phone', label: 'Phone' },
          { key: 'street', label: 'Street', required: true },
          { key: 'number', label: 'Number', required: true },
          { key: 'district', label: 'District', required: true },
          { key: 'city', label: 'City', required: true },
          { key: 'region', label: 'Region', required: true },
          { key: 'country', label: 'Country', required: true },
          { key: 'postalCode', label: 'Postal code', required: true },
        ],
        list: (criteria, page, size) =>
          this.companies
            .listCompanies({ criteria, pageable: { page, size } })
            .pipe(map((response) => ({ content: response.content ?? [], totalElements: response.totalElements ?? 0 }))),
        create: (value) =>
          this.companies.createCompany({
            legalName: text(value['legalName']),
            tradeName: text(value['tradeName']) || undefined,
            cnpj: text(value['cnpj']),
            type: text(value['type']) === 'CORRESPONDENT' ? 'CORRESPONDENT' : 'FACTORY',
            contact: text(value['email'])
              ? { email: text(value['email']), phone: text(value['phone']) || undefined }
              : undefined,
            address: {
              street: text(value['street']),
              number: text(value['number']),
              district: text(value['district']),
              city: text(value['city']),
              region: text(value['region']),
              country: text(value['country']),
              postalCode: text(value['postalCode']),
            },
          }),
        getById: (id) => this.companies.getCompanyById(id),
      },
      users: {
        key: 'users',
        title: 'Users',
        description: 'Application users and roles.',
        columns: [
          { key: 'displayName', label: 'Name' },
          { key: 'email', label: 'Email' },
          { key: 'role', label: 'Role' },
          { key: 'status', label: 'Status' },
        ],
        detailFields: [
          { key: 'displayName', label: 'Display name' },
          { key: 'email', label: 'Email' },
          { key: 'phone', label: 'Phone' },
          { key: 'birthDate', label: 'Birth date' },
          { key: 'role', label: 'Role' },
          { key: 'status', label: 'Status' },
          { key: 'companyId', label: 'Company ID' },
        ],
        searchFields: [
          { key: 'name', label: 'Name' },
          { key: 'email', label: 'Email', type: 'email' },
          { key: 'companyId', label: 'Company', type: 'select', optionsKey: 'companies' },
        ],
        createFields: [
          { key: 'firstName', label: 'First name', required: true },
          { key: 'lastName', label: 'Last name', required: true },
          { key: 'email', label: 'Email', required: true, type: 'email' },
          { key: 'phone', label: 'Phone' },
          { key: 'birthDate', label: 'Birth date' },
          { key: 'role', label: 'Role', required: true, type: 'select' },
          { key: 'companyId', label: 'Company', required: true, type: 'select', optionsKey: 'companies' },
          { key: 'password', label: 'Password', required: true, type: 'password' },
        ],
        list: (criteria, page, size) =>
          this.users
            .listUsers({ criteria, pageable: { page, size } })
            .pipe(map((response) => ({ content: response.content ?? [], totalElements: response.totalElements ?? 0 }))),
        create: (value) =>
          this.users.createUser({
            firstName: text(value['firstName']),
            lastName: text(value['lastName']),
            email: text(value['email']),
            phone: text(value['phone']) || undefined,
            birthDate: text(value['birthDate']) || undefined,
            role: text(value['role']) === 'MANAGER' ? 'MANAGER' : 'ADMIN',
            companyId: text(value['companyId']),
            password: text(value['password']),
          }),
        getById: (id) => this.users.getUserById(id),
      },
      categories: {
        key: 'categories',
        title: 'Categories',
        description: 'Product category records.',
        columns: [
          { key: 'code', label: 'Code' },
          { key: 'name', label: 'Name' },
          { key: 'status', label: 'Status' },
        ],
        detailFields: [
          { key: 'code', label: 'Code' },
          { key: 'name', label: 'Name' },
          { key: 'description', label: 'Description' },
          { key: 'status', label: 'Status' },
        ],
        searchFields: [
          { key: 'code', label: 'Code' },
          { key: 'name', label: 'Name' },
        ],
        createFields: [
          { key: 'code', label: 'Code', required: true },
          { key: 'name', label: 'Name', required: true },
          { key: 'description', label: 'Description' },
        ],
        list: (criteria, page, size) =>
          this.catalog
            .listCategories({ criteria, pageable: { page, size } })
            .pipe(map((response) => ({ content: response.content ?? [], totalElements: response.totalElements ?? 0 }))),
        create: (value) =>
          this.catalog.createCategory({
            code: text(value['code']),
            name: text(value['name']),
            description: text(value['description']) || undefined,
          }),
        getById: (id) => this.catalog.getCategoryById(id),
      },
      products: {
        key: 'products',
        title: 'Products',
        description: 'Sellable product records.',
        columns: [
          { key: 'sku', label: 'SKU' },
          { key: 'name', label: 'Name' },
          { key: 'category.name', label: 'Category' },
          { key: 'status', label: 'Status' },
        ],
        detailFields: [
          { key: 'sku', label: 'SKU' },
          { key: 'name', label: 'Name' },
          { key: 'description', label: 'Description' },
          { key: 'basePrice.amount', label: 'Price' },
          { key: 'basePrice.currencyCode', label: 'Currency' },
          { key: 'category.name', label: 'Category' },
          { key: 'status', label: 'Status' },
        ],
        searchFields: [
          { key: 'sku', label: 'SKU' },
          { key: 'name', label: 'Name' },
          { key: 'categoryId', label: 'Category', type: 'select', optionsKey: 'categories' },
        ],
        createFields: [
          { key: 'sku', label: 'SKU', required: true },
          { key: 'name', label: 'Name', required: true },
          { key: 'description', label: 'Description' },
          { key: 'amount', label: 'Base price', required: true, type: 'number' },
          { key: 'currencyCode', label: 'Currency', required: true },
          { key: 'categoryId', label: 'Category', required: true, type: 'select', optionsKey: 'categories' },
        ],
        list: (criteria, page, size) =>
          this.catalog
            .listProducts({ criteria, pageable: { page, size } })
            .pipe(map((response) => ({ content: response.content ?? [], totalElements: response.totalElements ?? 0 }))),
        create: (value) =>
          this.catalog.createProduct({
            sku: text(value['sku']),
            name: text(value['name']),
            description: text(value['description']) || undefined,
            basePrice: {
              amount: Number(value['amount']),
              currencyCode: text(value['currencyCode']),
            },
            categoryId: text(value['categoryId']),
          }),
        getById: (id) => this.catalog.getProductById(id),
      },
      customers: {
        key: 'customers',
        title: 'Customers',
        description: 'Customer accounts.',
        columns: [
          { key: 'name', label: 'Name' },
          { key: 'document', label: 'Document' },
          { key: 'contact.email', label: 'Email' },
        ],
        detailFields: [
          { key: 'name', label: 'Name' },
          { key: 'document', label: 'Document' },
          { key: 'companyId', label: 'Company ID' },
          { key: 'contact.email', label: 'Email' },
          { key: 'contact.phone', label: 'Phone' },
          { key: 'address.city', label: 'City' },
          { key: 'address.country', label: 'Country' },
        ],
        searchFields: partySearch,
        createFields: partyFields,
        list: (criteria, page, size) =>
          this.customers
            .listCustomers({ criteria, pageable: { page, size } })
            .pipe(map((response) => ({ content: response.content ?? [], totalElements: response.totalElements ?? 0 }))),
        create: (value) =>
          this.customers.createCustomer({
            name: text(value['name']),
            document: text(value['document']),
            companyId: text(value['companyId']),
            contact: text(value['email'])
              ? { email: text(value['email']), phone: text(value['phone']) || undefined }
              : undefined,
            address: text(value['street']) || text(value['city']) || text(value['country'])
              ? {
                  street: text(value['street']),
                  number: text(value['number']),
                  district: text(value['district']),
                  city: text(value['city']),
                  region: text(value['region']),
                  country: text(value['country']),
                  postalCode: text(value['postalCode']),
                }
              : undefined,
          }),
        getById: (id) => this.customers.getCustomerById(id),
      },
      suppliers: {
        key: 'suppliers',
        title: 'Suppliers',
        description: 'Supplier accounts.',
        columns: [
          { key: 'name', label: 'Name' },
          { key: 'document', label: 'Document' },
          { key: 'contact.email', label: 'Email' },
        ],
        detailFields: [
          { key: 'name', label: 'Name' },
          { key: 'document', label: 'Document' },
          { key: 'companyId', label: 'Company ID' },
          { key: 'contact.email', label: 'Email' },
          { key: 'contact.phone', label: 'Phone' },
          { key: 'address.city', label: 'City' },
          { key: 'address.country', label: 'Country' },
        ],
        searchFields: partySearch,
        createFields: partyFields,
        list: (criteria, page, size) =>
          this.suppliers
            .listSuppliers({ criteria, pageable: { page, size } })
            .pipe(map((response) => ({ content: response.content ?? [], totalElements: response.totalElements ?? 0 }))),
        create: (value) =>
          this.suppliers.createSupplier({
            name: text(value['name']),
            document: text(value['document']),
            companyId: text(value['companyId']),
            contact: text(value['email'])
              ? { email: text(value['email']), phone: text(value['phone']) || undefined }
              : undefined,
            address: text(value['street']) || text(value['city']) || text(value['country'])
              ? {
                  street: text(value['street']),
                  number: text(value['number']),
                  district: text(value['district']),
                  city: text(value['city']),
                  region: text(value['region']),
                  country: text(value['country']),
                  postalCode: text(value['postalCode']),
                }
              : undefined,
          }),
        getById: (id) => this.suppliers.getSupplierById(id),
      },
      'contact-persons': {
        key: 'contact-persons',
        title: 'Contact persons',
        description: 'People attached to customer accounts.',
        columns: [
          { key: 'name', label: 'Name' },
          { key: 'customerId', label: 'Customer ID' },
          { key: 'contact.email', label: 'Email' },
        ],
        detailFields: [
          { key: 'name', label: 'Name' },
          { key: 'customerId', label: 'Customer ID' },
          { key: 'contact.email', label: 'Email' },
          { key: 'contact.phone', label: 'Phone' },
        ],
        searchFields: [],
        createFields: [
          { key: 'name', label: 'Name', required: true },
          { key: 'customerId', label: 'Customer', required: true, type: 'select', optionsKey: 'customers' },
          { key: 'email', label: 'Email', required: true, type: 'email' },
          { key: 'phone', label: 'Phone' },
        ],
        list: (_criteria, page, size) =>
          this.contactPersons
            .listContactPersons({ pageable: { page, size } })
            .pipe(map((response) => ({ content: response.content ?? [], totalElements: response.totalElements ?? 0 }))),
        create: (value) =>
          this.contactPersons.createContactPerson({
            name: text(value['name']),
            customerId: text(value['customerId']),
            contact: { email: text(value['email']), phone: text(value['phone']) || undefined },
          }),
        getById: (id) => this.contactPersons.getContactPersonById(id),
      },
    };
  }
}
