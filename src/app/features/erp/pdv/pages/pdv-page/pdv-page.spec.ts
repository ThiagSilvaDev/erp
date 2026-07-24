import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { NzMessageService } from 'ng-zorro-antd/message';

import { CatalogService } from '@generated-api/modules/catalog/Catalog.service';
import { CompaniesService } from '@generated-api/modules/companies/Companies.service';
import { CustomersService } from '@generated-api/modules/customers/Customers.service';
import { InventoryService } from '@generated-api/modules/inventory/Inventory.service';
import { SalesService } from '@generated-api/modules/sales/Sales.service';
import {
  CreateSalesOrderRequestFulfillmentType,
  CreateSalesOrderRequestPaymentMethod,
  CreateSalesOrderRequestSaleType,
} from '@generated-api/modules/sales/dtos';
import { PdvPage } from './pdv-page';

function page(content: object[] = []) {
  return of({ content, page: { totalElements: content.length } });
}

describe('PdvPage', () => {
  const products = [
    {
      id: 'product-1',
      sku: 'SKU-1',
      name: 'Product 1',
      basePrice: { amount: 10, currencyCode: 'BRL' },
      status: 'ACTIVE' as const,
    },
    {
      id: 'product-2',
      sku: 'SKU-2',
      name: 'Product 2',
      basePrice: { amount: 20, currencyCode: 'BRL' },
      status: 'ACTIVE' as const,
    },
    {
      id: 'product-3',
      sku: 'SKU-3',
      name: 'Product 3',
      basePrice: { amount: 30, currencyCode: 'USD' },
      status: 'ACTIVE' as const,
    },
  ];
  const catalog = {
    listProducts: vi.fn(() => page(products)),
  };
  const companies = {
    getCompanyAddress: vi.fn(() =>
      of({
        companyId: 'company-2',
        companyName: 'Buyer',
        deliveryRouteDestinationDescription: 'Buyer address',
      }),
    ),
    listCompanies: vi.fn(() =>
      page([
        { id: 'company-1', tradeName: 'Seller' },
        { id: 'company-2', tradeName: 'Buyer' },
      ]),
    ),
  };
  const customers = {
    getCustomerById: vi.fn(() =>
      of({
        id: 'customer-1',
        name: 'Customer',
        ownerCompanyId: 'company-2',
      }),
    ),
    listCustomers: vi.fn(() => page([{ id: 'customer-1', name: 'Customer' }])),
  };
  const inventory = {
    listWarehouses: vi.fn(() => page([{ id: 'warehouse-1', name: 'Main' }])),
  };
  const messages = {
    error: vi.fn(),
    success: vi.fn(),
    warning: vi.fn(),
  };
  const sales = {
    confirmSalesOrder: vi.fn(() => of({ id: 'order-1', status: 'CONFIRMED' })),
    createSalesOrder: vi.fn(() => of({ id: 'order-1', status: 'DRAFT' })),
  };

  function createComponent(): ComponentFixture<PdvPage> {
    TestBed.configureTestingModule({
      imports: [PdvPage],
      providers: [
        provideNoopAnimations(),
        { provide: CatalogService, useValue: catalog },
        { provide: CompaniesService, useValue: companies },
        { provide: CustomersService, useValue: customers },
        { provide: InventoryService, useValue: inventory },
        { provide: NzMessageService, useValue: messages },
        { provide: SalesService, useValue: sales },
      ],
    });

    return TestBed.createComponent(PdvPage);
  }

  beforeEach(() => {
    TestBed.resetTestingModule();
    vi.clearAllMocks();
  });

  it('loads products and lookup options on entry', () => {
    createComponent();

    expect(catalog.listProducts).toHaveBeenCalledWith({
      sku: undefined,
      name: undefined,
      status: 'ACTIVE',
      page: 0,
      size: 12,
    });
    expect(companies.listCompanies).toHaveBeenCalledWith({ page: 0, size: 50 });
    expect(customers.listCustomers).toHaveBeenCalledWith({ page: 0, size: 50 });
    expect(inventory.listWarehouses).toHaveBeenCalledWith({ page: 0, size: 50 });
  });

  it('loads delivery route details from the selected customer owner company', () => {
    const component = createComponent().componentInstance;

    component.saleForm.patchValue({ customerId: 'customer-1' });

    expect(customers.getCustomerById).toHaveBeenCalledWith('customer-1');
    expect(companies.getCompanyAddress).toHaveBeenCalledWith('company-2');
    expect(component.saleForm.controls.deliveryRouteName.value).toBe('Buyer delivery');
    expect(component.saleForm.controls.deliveryRouteDestinationDescription.value).toBe(
      'Buyer address',
    );
  });

  it('searches products with typed query params', () => {
    const component = createComponent().componentInstance;

    component.productSearchForm.setValue({ sku: 'SKU-1', name: 'Product' });
    component.submitProductSearch();

    expect(catalog.listProducts).toHaveBeenLastCalledWith({
      sku: 'SKU-1',
      name: 'Product',
      status: 'ACTIVE',
      page: 0,
      size: 12,
    });
  });

  it('increments, removes, and clears cart items', () => {
    const component = createComponent().componentInstance;

    component.addToCart(products[0]);
    component.addToCart(products[0]);
    component.addToCart(products[1]);

    expect(component.cartItems()).toEqual([
      { product: products[0], quantity: 2 },
      { product: products[1], quantity: 1 },
    ]);

    component.removeFromCart('product-1');
    expect(component.cartItems()).toEqual([{ product: products[1], quantity: 1 }]);

    component.clearCart();
    expect(component.cartItems()).toEqual([]);
  });

  it('rejects mixed-currency products', () => {
    const component = createComponent().componentInstance;

    component.addToCart(products[0]);
    component.addToCart(products[2]);

    expect(component.cartItems()).toEqual([{ product: products[0], quantity: 1 }]);
    expect(messages.warning).toHaveBeenCalledWith(
      'Products with different currencies cannot be mixed in one sale.',
    );
  });

  it('creates a B2C sale and confirms it', () => {
    const component = createComponent().componentInstance;

    component.saleForm.patchValue({
      targetType: 'customer',
      customerId: 'customer-1',
      warehouseId: 'warehouse-1',
    });
    component.addToCart(products[0]);
    component.addToCart(products[1]);
    component.changeQuantity('product-1', 3);

    component.checkout();

    expect(sales.createSalesOrder).toHaveBeenCalledWith({
      saleType: CreateSalesOrderRequestSaleType.B2C,
      buyerCompanyId: undefined,
      customerId: 'customer-1',
      paymentMethod: CreateSalesOrderRequestPaymentMethod.CASH,
      fulfillmentType: CreateSalesOrderRequestFulfillmentType.PICKUP,
      deliveryRoute: undefined,
      items: [
        { productId: 'product-1', warehouseId: 'warehouse-1', quantity: 3 },
        { productId: 'product-2', warehouseId: 'warehouse-1', quantity: 1 },
      ],
    });
    expect(sales.confirmSalesOrder).toHaveBeenCalledWith('order-1');
  });

  it('creates a B2B sale with buyer company and confirms it', () => {
    const component = createComponent().componentInstance;

    component.saleForm.patchValue({
      targetType: 'company',
      buyerCompanyId: 'company-2',
      warehouseId: 'warehouse-1',
    });
    component.setTargetType('company');
    component.addToCart(products[0]);

    component.checkout();

    expect(sales.createSalesOrder).toHaveBeenCalledWith({
      saleType: CreateSalesOrderRequestSaleType.B2B,
      buyerCompanyId: 'company-2',
      customerId: undefined,
      paymentMethod: CreateSalesOrderRequestPaymentMethod.CASH,
      fulfillmentType: CreateSalesOrderRequestFulfillmentType.PICKUP,
      deliveryRoute: undefined,
      items: [{ productId: 'product-1', warehouseId: 'warehouse-1', quantity: 1 }],
    });
    expect(sales.confirmSalesOrder).toHaveBeenCalledWith('order-1');
  });

  it('creates a delivery sale with delivery route details', () => {
    const component = createComponent().componentInstance;

    component.saleForm.patchValue({
      targetType: 'customer',
      customerId: 'customer-1',
      warehouseId: 'warehouse-1',
      fulfillmentType: CreateSalesOrderRequestFulfillmentType.DELIVERY,
      deliveryRouteName: 'Route A',
      deliveryRouteDestinationDescription: 'Customer loading dock',
    });
    component.addToCart(products[0]);

    component.checkout();

    expect(sales.createSalesOrder).toHaveBeenCalledWith({
      saleType: CreateSalesOrderRequestSaleType.B2C,
      buyerCompanyId: undefined,
      customerId: 'customer-1',
      paymentMethod: CreateSalesOrderRequestPaymentMethod.CASH,
      fulfillmentType: CreateSalesOrderRequestFulfillmentType.DELIVERY,
      deliveryRoute: {
        routeName: 'Route A',
        originWarehouseId: 'warehouse-1',
        destinationDescription: 'Customer loading dock',
      },
      items: [{ productId: 'product-1', warehouseId: 'warehouse-1', quantity: 1 }],
    });
    expect(sales.confirmSalesOrder).toHaveBeenCalledWith('order-1');
  });
});
