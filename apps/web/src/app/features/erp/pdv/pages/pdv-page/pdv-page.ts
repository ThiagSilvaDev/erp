import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, forkJoin, of, switchMap, throwError } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzRadioModule } from 'ng-zorro-antd/radio';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';

import { CatalogService } from '@generated-api/modules/catalog/Catalog.service';
import { ListProductsStatus } from '@generated-api/modules/catalog/dtos';
import type { ProductResponse } from '@generated-api/modules/catalog/dtos';
import { CompaniesService } from '@generated-api/modules/companies/Companies.service';
import type {
  AddressResponse as CompanyAddress,
  CompanyAddressResponse,
  CompanyResponse,
} from '@generated-api/modules/companies/dtos';
import { CustomersService } from '@generated-api/modules/customers/Customers.service';
import type { CustomerResponse } from '@generated-api/modules/customers/dtos';
import { AuthService } from '@core/auth/auth.service';
import { InventoryService } from '@generated-api/modules/inventory/Inventory.service';
import type { WarehouseResponse } from '@generated-api/modules/inventory/dtos';
import { SalesService } from '@generated-api/modules/sales/Sales.service';
import {
  CreateSalesOrderRequestFulfillmentType,
  CreateSalesOrderRequestPaymentMethod,
  CreateSalesOrderRequestSaleType,
} from '@generated-api/modules/sales/dtos';

type PdvTargetType = 'customer' | 'company';

type SaleForm = FormGroup<{
  targetType: FormControl<PdvTargetType>;
  customerId: FormControl<string>;
  buyerCompanyId: FormControl<string>;
  warehouseId: FormControl<string>;
  paymentMethod: FormControl<CreateSalesOrderRequestPaymentMethod>;
  fulfillmentType: FormControl<CreateSalesOrderRequestFulfillmentType>;
  deliveryRouteName: FormControl<string>;
  deliveryRouteDestinationDescription: FormControl<string>;
}>;

type ProductSearchForm = FormGroup<{
  sku: FormControl<string>;
  name: FormControl<string>;
}>;

interface CartItem {
  product: ProductResponse;
  quantity: number;
}

interface SelectOption {
  label: string;
  value: string;
}

interface TypedSelectOption<TValue extends string> {
  label: string;
  value: TValue;
}

const PAGE_SIZE = 12;
const LOOKUP_SIZE = 50;
const PRODUCT_IMAGE_PATHS = [
  '/images/pdv/product-grocery.webp',
  '/images/pdv/product-electronics.webp',
  '/images/pdv/product-office.webp',
  '/images/pdv/product-home-care.webp',
  '/images/pdv/product-apparel.webp',
  '/images/pdv/product-tools.webp',
] as const;
const PRODUCT_IMAGE_RULES: readonly { keywords: readonly string[]; path: string }[] = [
  {
    keywords: [
      'tool',
      'hardware',
      'industrial',
      'construction',
      'safety',
      'ferramenta',
      'construcao',
      'seguranca',
      'capacete',
      'furadeira',
    ],
    path: '/images/pdv/product-tools.webp',
  },
  {
    keywords: [
      'electronic',
      'computer',
      'technology',
      'phone',
      'audio',
      'informatica',
      'celular',
      'teclado',
      'mouse',
      'fone',
      'cabo',
    ],
    path: '/images/pdv/product-electronics.webp',
  },
  {
    keywords: [
      'grocery',
      'food',
      'beverage',
      'pantry',
      'market',
      'alimento',
      'bebida',
      'mercado',
      'cozinha',
      'arroz',
      'massa',
    ],
    path: '/images/pdv/product-grocery.webp',
  },
  {
    keywords: [
      'cleaning',
      'cleaner',
      'hygiene',
      'detergent',
      'household',
      'limpeza',
      'higiene',
      'detergente',
      'sabao',
    ],
    path: '/images/pdv/product-home-care.webp',
  },
  {
    keywords: [
      'apparel',
      'clothing',
      'fashion',
      'shoe',
      'shirt',
      'roupa',
      'calcado',
      'tenis',
      'vestuario',
      'camisa',
    ],
    path: '/images/pdv/product-apparel.webp',
  },
  {
    keywords: [
      'office',
      'stationery',
      'notebook',
      'paper',
      'escritorio',
      'papelaria',
      'caderno',
      'papel',
      'caneta',
    ],
    path: '/images/pdv/product-office.webp',
  },
];
const PAYMENT_METHOD_OPTIONS: TypedSelectOption<CreateSalesOrderRequestPaymentMethod>[] = [
  { label: 'Cash', value: CreateSalesOrderRequestPaymentMethod.CASH },
  { label: 'PIX', value: CreateSalesOrderRequestPaymentMethod.PIX },
  { label: 'Credit card', value: CreateSalesOrderRequestPaymentMethod.CREDIT_CARD },
  { label: 'Debit card', value: CreateSalesOrderRequestPaymentMethod.DEBIT_CARD },
  { label: 'Bank transfer', value: CreateSalesOrderRequestPaymentMethod.BANK_TRANSFER },
  { label: 'Boleto', value: CreateSalesOrderRequestPaymentMethod.BOLETO },
];
const FULFILLMENT_TYPE_OPTIONS: TypedSelectOption<CreateSalesOrderRequestFulfillmentType>[] = [
  { label: 'Pickup', value: CreateSalesOrderRequestFulfillmentType.PICKUP },
  { label: 'Delivery', value: CreateSalesOrderRequestFulfillmentType.DELIVERY },
];

@Component({
  selector: 'app-pdv-page',
  imports: [
    ReactiveFormsModule,
    NzAlertModule,
    NzButtonModule,
    NzFormModule,
    NzIconModule,
    NzInputModule,
    NzPaginationModule,
    NzRadioModule,
    NzSelectModule,
    NzSpinModule,
    NzTableModule,
    NzTagModule,
  ],
  templateUrl: './pdv-page.html',
  styleUrl: './pdv-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PdvPage {
  private readonly auth = inject(AuthService);
  private readonly catalog = inject(CatalogService);
  private readonly companies = inject(CompaniesService);
  private readonly customers = inject(CustomersService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly inventory = inject(InventoryService);
  private readonly messages = inject(NzMessageService);
  private readonly sales = inject(SalesService);

  readonly saleForm: SaleForm = new FormGroup({
    targetType: new FormControl<PdvTargetType>('customer', { nonNullable: true }),
    customerId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    buyerCompanyId: new FormControl('', { nonNullable: true }),
    warehouseId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    paymentMethod: new FormControl<CreateSalesOrderRequestPaymentMethod>(
      CreateSalesOrderRequestPaymentMethod.CASH,
      {
        nonNullable: true,
        validators: [Validators.required],
      },
    ),
    fulfillmentType: new FormControl<CreateSalesOrderRequestFulfillmentType>(
      CreateSalesOrderRequestFulfillmentType.PICKUP,
      {
        nonNullable: true,
        validators: [Validators.required],
      },
    ),
    deliveryRouteName: new FormControl('', { nonNullable: true }),
    deliveryRouteDestinationDescription: new FormControl('', { nonNullable: true }),
  });

  readonly productSearchForm: ProductSearchForm = new FormGroup({
    sku: new FormControl('', { nonNullable: true }),
    name: new FormControl('', { nonNullable: true }),
  });

  readonly products = signal<ProductResponse[]>([]);
  readonly productsLoading = signal(false);
  readonly productsError = signal<string | null>(null);
  readonly productsPageIndex = signal(1);
  readonly productsTotal = signal(0);

  readonly companiesLoading = signal(false);
  readonly customersLoading = signal(false);
  readonly warehousesLoading = signal(false);
  readonly lookupError = signal<string | null>(null);
  readonly companyOptions = signal<SelectOption[]>([]);
  readonly customerOptions = signal<SelectOption[]>([]);
  readonly warehouseOptions = signal<SelectOption[]>([]);
  readonly paymentMethodOptions = PAYMENT_METHOD_OPTIONS;
  readonly fulfillmentTypeOptions = FULFILLMENT_TYPE_OPTIONS;

  readonly cartItems = signal<CartItem[]>([]);
  readonly isCheckingOut = signal(false);
  private readonly saleFormRevision = signal(0);

  readonly pageSize = PAGE_SIZE;
  readonly targetType = signal<PdvTargetType>('customer');
  readonly isLookupLoading = computed(
    () => this.companiesLoading() || this.customersLoading() || this.warehousesLoading(),
  );
  readonly cartCurrency = computed(
    () => this.cartItems()[0]?.product.basePrice?.currencyCode ?? '',
  );
  readonly cartSubtotal = computed(() =>
    this.cartItems().reduce(
      (total, item) => total + this.productAmount(item.product) * item.quantity,
      0,
    ),
  );
  readonly cartQuantity = computed(() =>
    this.cartItems().reduce((total, item) => total + item.quantity, 0),
  );
  readonly canCheckout = computed(() => {
    this.saleFormRevision();
    return this.cartItems().length > 0 && this.saleForm.valid;
  });
  readonly isDelivery = computed(() => {
    this.saleFormRevision();
    return (
      this.saleForm.controls.fulfillmentType.value ===
      CreateSalesOrderRequestFulfillmentType.DELIVERY
    );
  });

  constructor() {
    this.saleForm.controls.targetType.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((targetType) => this.setTargetType(targetType));
    this.saleForm.controls.fulfillmentType.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.updateDeliveryRouteValidators());
    this.saleForm.controls.customerId.valueChanges
      .pipe(
        switchMap((customerId) => this.customerDeliveryAddress(this.text(customerId))),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((address) => this.applyDeliveryAddress(address));
    this.saleForm.controls.buyerCompanyId.valueChanges
      .pipe(
        switchMap((companyId) => this.companyDeliveryAddress(this.text(companyId))),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((address) => this.applyDeliveryAddress(address));
    this.saleForm.statusChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.saleFormRevision.update((revision) => revision + 1));

    this.updateDeliveryRouteValidators();
    this.loadLookups();
    this.loadProducts();
  }

  submitProductSearch(): void {
    this.productsPageIndex.set(1);
    this.loadProducts();
  }

  resetProductSearch(): void {
    this.productSearchForm.reset();
    this.submitProductSearch();
  }

  changeProductsPage(page: number): void {
    this.productsPageIndex.set(page);
    this.loadProducts();
  }

  setTargetType(targetType: PdvTargetType): void {
    this.targetType.set(targetType);

    if (targetType === 'customer') {
      this.saleForm.controls.customerId.setValidators([Validators.required]);
      this.saleForm.controls.buyerCompanyId.clearValidators();
      this.saleForm.controls.buyerCompanyId.setValue('');
    } else {
      this.saleForm.controls.buyerCompanyId.setValidators([Validators.required]);
      this.saleForm.controls.customerId.clearValidators();
      this.saleForm.controls.customerId.setValue('');
    }

    this.saleForm.controls.customerId.updateValueAndValidity();
    this.saleForm.controls.buyerCompanyId.updateValueAndValidity();
  }

  updateDeliveryRouteValidators(): void {
    const isDelivery =
      this.saleForm.controls.fulfillmentType.value ===
      CreateSalesOrderRequestFulfillmentType.DELIVERY;
    const routeControls = [
      this.saleForm.controls.deliveryRouteName,
      this.saleForm.controls.deliveryRouteDestinationDescription,
    ];

    for (const control of routeControls) {
      if (isDelivery) {
        control.setValidators([Validators.required]);
      } else {
        control.clearValidators();
        control.setValue('');
      }
      control.updateValueAndValidity();
    }

    this.saleFormRevision.update((revision) => revision + 1);
  }

  addToCart(product: ProductResponse): void {
    const productId = this.text(product.id);
    const currency = product.basePrice?.currencyCode ?? '';
    const existingCurrency = this.cartCurrency();

    if (!productId) {
      this.messages.warning('This product cannot be added because it has no ID.');
      return;
    }

    if (existingCurrency && currency && existingCurrency !== currency) {
      this.messages.warning('Products with different currencies cannot be mixed in one sale.');
      return;
    }

    this.cartItems.update((items) => {
      const existing = items.find((item) => item.product.id === productId);
      if (existing) {
        return items.map((item) =>
          item.product.id === productId ? { ...item, quantity: item.quantity + 1 } : item,
        );
      }

      return [...items, { product, quantity: 1 }];
    });
  }

  changeQuantity(productId: string | undefined, rawQuantity: string | number): void {
    const id = this.text(productId);
    const quantity = Math.max(1, Number(rawQuantity) || 1);

    this.cartItems.update((items) =>
      items.map((item) => (item.product.id === id ? { ...item, quantity } : item)),
    );
  }

  removeFromCart(productId: string | undefined): void {
    const id = this.text(productId);
    this.cartItems.update((items) => items.filter((item) => item.product.id !== id));
  }

  clearCart(): void {
    this.cartItems.set([]);
  }

  checkout(): void {
    if (!this.canCheckout()) {
      this.saleForm.markAllAsTouched();
      return;
    }

    const value = this.saleForm.getRawValue();
    const targetType = value.targetType;
    this.isCheckingOut.set(true);

    this.sales
      .createSalesOrder({
        saleType:
          targetType === 'company'
            ? CreateSalesOrderRequestSaleType.B2B
            : CreateSalesOrderRequestSaleType.B2C,
        buyerCompanyId: targetType === 'company' ? value.buyerCompanyId : undefined,
        customerId: targetType === 'customer' ? value.customerId : undefined,
        paymentMethod: value.paymentMethod,
        fulfillmentType: value.fulfillmentType,
        deliveryRoute:
          value.fulfillmentType === CreateSalesOrderRequestFulfillmentType.DELIVERY
            ? {
                routeName: value.deliveryRouteName,
                originWarehouseId: value.warehouseId,
                destinationDescription: value.deliveryRouteDestinationDescription,
              }
            : undefined,
        items: this.cartItems().map((item) => ({
          productId: this.text(item.product.id),
          warehouseId: value.warehouseId,
          quantity: item.quantity,
        })),
      })
      .pipe(
        switchMap((order) => {
          if (!order.id) {
            return throwError(() => new Error('Created order did not include an ID.'));
          }
          return this.sales.confirmSalesOrder(order.id);
        }),
        finalize(() => this.isCheckingOut.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.messages.success('Sale confirmed.');
          this.resetSale();
        },
        error: () => this.messages.error('Sale was created but could not be confirmed.'),
      });
  }

  formatMoney(product: ProductResponse): string {
    const amount = product.basePrice?.amount;
    const currency = product.basePrice?.currencyCode;
    if (typeof amount !== 'number') {
      return '-';
    }

    return `${amount.toFixed(2)}${currency ? ` ${currency}` : ''}`;
  }

  formatTotal(): string {
    const currency = this.cartCurrency();
    return `${this.cartSubtotal().toFixed(2)}${currency ? ` ${currency}` : ''}`;
  }

  productAmount(product: ProductResponse): number {
    return product.basePrice?.amount ?? 0;
  }

  lineTotal(item: CartItem): string {
    const total = this.productAmount(item.product) * item.quantity;
    const currency = item.product.basePrice?.currencyCode;
    return `${total.toFixed(2)}${currency ? ` ${currency}` : ''}`;
  }

  productLabel(product: ProductResponse): string {
    return product.name || product.sku || product.id || 'Product';
  }

  productImage(product: ProductResponse): string {
    const searchableText = [
      product.category?.name,
      product.category?.code,
      product.name,
      product.description,
      product.sku,
    ]
      .filter(Boolean)
      .join(' ')
      .normalize('NFD')
      .replace(/\p{Diacritic}/gu, '')
      .toLowerCase();
    const match = PRODUCT_IMAGE_RULES.find((rule) =>
      rule.keywords.some((keyword) => searchableText.includes(keyword)),
    );

    if (match) {
      return match.path;
    }

    const stableKey = this.text(product.id) || this.text(product.sku) || this.productLabel(product);
    const imageIndex = [...stableKey].reduce(
      (hash, character) => (hash * 31 + character.charCodeAt(0)) >>> 0,
      0,
    );
    return PRODUCT_IMAGE_PATHS[imageIndex % PRODUCT_IMAGE_PATHS.length];
  }

  text(value: unknown): string {
    if (typeof value === 'string' || typeof value === 'number') {
      return String(value);
    }
    return '';
  }

  private loadProducts(): void {
    const search = this.productSearchForm.getRawValue();
    this.productsLoading.set(true);
    this.productsError.set(null);

    this.catalog
      .listProducts({
        sku: search.sku.trim() || undefined,
        name: search.name.trim() || undefined,
        status: ListProductsStatus.ACTIVE,
        page: this.productsPageIndex() - 1,
        size: PAGE_SIZE,
      })
      .pipe(
        finalize(() => this.productsLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (page) => {
          this.products.set(page.content ?? []);
          this.productsTotal.set(page.page?.totalElements ?? 0);
        },
        error: () => {
          this.products.set([]);
          this.productsTotal.set(0);
          this.productsError.set('Unable to load products.');
        },
      });
  }

  private customerDeliveryAddress(customerId: string) {
    if (!customerId || this.targetType() !== 'customer') {
      return of<CompanyAddressResponse | null>(null);
    }

    return this.customers.getCustomerById(customerId).pipe(
      switchMap((customer) => {
        const ownerCompanyId = this.text(customer.ownerCompanyId);
        if (ownerCompanyId) {
          return this.loadCompanyDeliveryAddress(ownerCompanyId);
        }

        return of(this.companyAddressFromCustomer(customer));
      }),
      catchError(() => {
        this.messages.warning('Unable to load the customer delivery address.');
        return of<CompanyAddressResponse | null>(null);
      }),
    );
  }

  private companyDeliveryAddress(companyId: string) {
    if (!companyId || this.targetType() !== 'company') {
      return of<CompanyAddressResponse | null>(null);
    }

    return this.loadCompanyDeliveryAddress(companyId);
  }

  private loadCompanyDeliveryAddress(companyId: string) {
    if (!companyId) {
      return of<CompanyAddressResponse | null>(null);
    }

    return this.companies.getCompanyAddress(companyId).pipe(
      catchError(() => {
        this.messages.warning('Unable to load the company delivery address.');
        return of<CompanyAddressResponse | null>(null);
      }),
    );
  }

  private companyAddressFromCustomer(customer: CustomerResponse): CompanyAddressResponse {
    return {
      companyId: this.text(customer.ownerCompanyId),
      companyName: customer.name,
      address: customer.address as CompanyAddress | undefined,
      deliveryRouteDestinationDescription: this.formatAddress(customer.address),
    };
  }

  private applyDeliveryAddress(address: CompanyAddressResponse | null): void {
    this.saleForm.patchValue({
      deliveryRouteName: address?.companyName ? `${address.companyName} delivery` : '',
      deliveryRouteDestinationDescription:
        address?.deliveryRouteDestinationDescription || this.formatAddress(address?.address),
    });
  }

  private formatAddress(address: CompanyAddress | CustomerResponse['address'] | undefined): string {
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

  private loadLookups(): void {
    this.companiesLoading.set(true);
    this.customersLoading.set(true);
    this.warehousesLoading.set(true);
    this.lookupError.set(null);

    forkJoin({
      companies: this.companies.listCompanies({ page: 0, size: LOOKUP_SIZE }),
      customers: this.customers.listCustomers({ page: 0, size: LOOKUP_SIZE }),
      warehouses: this.inventory.listWarehouses({ page: 0, size: LOOKUP_SIZE }),
    })
      .pipe(
        finalize(() => {
          this.companiesLoading.set(false);
          this.customersLoading.set(false);
          this.warehousesLoading.set(false);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ({ companies, customers, warehouses }) => {
          const activeCompanyId = this.auth.session()?.activeCompany?.id;
          this.companyOptions.set(
            (companies.content ?? [])
              .filter((company) => company.id !== activeCompanyId)
              .map((company) => this.companyOption(company)),
          );
          this.customerOptions.set(
            (customers.content ?? []).map((customer) => this.customerOption(customer)),
          );
          this.warehouseOptions.set(
            (warehouses.content ?? []).map((warehouse) => this.warehouseOption(warehouse)),
          );
        },
        error: () => this.lookupError.set('Unable to load buyer, customer, or warehouse options.'),
      });
  }

  private resetSale(): void {
    this.cartItems.set([]);
    this.saleForm.patchValue({
      customerId: '',
      buyerCompanyId: '',
    });
  }

  private companyOption(company: CompanyResponse): SelectOption {
    return {
      value: this.text(company.id),
      label: company.tradeName || company.legalName || this.text(company.id) || 'Company',
    };
  }

  private customerOption(customer: CustomerResponse): SelectOption {
    return {
      value: this.text(customer.id),
      label: customer.name || customer.document || this.text(customer.id) || 'Customer',
    };
  }

  private warehouseOption(warehouse: WarehouseResponse): SelectOption {
    return {
      value: this.text(warehouse.id),
      label: warehouse.name || warehouse.code || this.text(warehouse.id) || 'Warehouse',
    };
  }
}
