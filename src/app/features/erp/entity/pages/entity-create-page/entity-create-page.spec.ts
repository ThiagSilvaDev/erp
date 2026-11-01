import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { Router } from '@angular/router';
import { of } from 'rxjs';

import { CatalogService } from '@generated-api/modules/catalog/Catalog.service';
import { CompaniesService } from '@generated-api/modules/companies/Companies.service';
import { ContactPersonsService } from '@generated-api/modules/contact-persons/ContactPersons.service';
import { CustomersService } from '@generated-api/modules/customers/Customers.service';
import { FinancialService } from '@generated-api/modules/financial/Financial.service';
import { InventoryService } from '@generated-api/modules/inventory/Inventory.service';
import { SalesService } from '@generated-api/modules/sales/Sales.service';
import { SuppliersService } from '@generated-api/modules/suppliers/Suppliers.service';
import { UsersService } from '@generated-api/modules/users/Users.service';
import { productsEntityConfig } from '../../../catalog/catalog.entity-config';
import { categoriesEntityConfig } from '../../../catalog/catalog.entity-config';
import { companiesEntityConfig } from '../../../companies/companies.entity-config';
import { salesOrdersEntityConfig } from '../../../sales/sales.entity-config';
import { usersEntityConfig } from '../../../users/users.entity-config';
import { CepAddressService } from '../../data-access/cep-address.service';
import { EntityOptionsFacade } from '../../data-access/entity-options.facade';
import { EntityRegistryService } from '../../data-access/entity-registry.service';
import { ENTITY_CONFIGS } from '../../tokens/entity.tokens';
import { EntityCreatePage } from './entity-create-page';

function paged(content: object[] = []) {
  return of({ content, page: { totalElements: content.length } });
}

describe('EntityCreatePage', () => {
  const catalog = {
    createProduct: vi.fn(() => of({ id: 'product-1' })),
    listCategories: vi.fn(() => paged()),
    listProducts: vi.fn(() => paged()),
  };
  const companies = {
    createCompany: vi.fn(() => of({ id: 'company-1' })),
    getCompanyAddress: vi.fn(() =>
      of({
        companyId: 'company-1',
        companyName: 'ACME',
        deliveryRouteDestinationDescription: 'ACME address',
      }),
    ),
    listCompanies: vi.fn(() => paged()),
  };
  const cepAddress = {
    lookup: vi.fn(() =>
      of({
        street: 'Praca da Se',
        district: 'Se',
        city: 'Sao Paulo',
        region: 'SP',
        postalCode: '01001-000',
        country: 'Brazil',
      }),
    ),
    normalizeCep: (cep: string) => cep.replace(/\D/g, ''),
  };
  const contactPersons = { listContactPersons: vi.fn(() => paged()) };
  const customers = {
    createCustomer: vi.fn(() => of({ id: 'customer-1' })),
    getCustomerById: vi.fn(() =>
      of({ id: 'customer-1', name: 'Customer', ownerCompanyId: 'company-1' }),
    ),
    listCustomers: vi.fn(() => paged()),
  };
  const financial = {
    listFinancialObligations: vi.fn(() => paged()),
    listInvoices: vi.fn(() => paged()),
    listPayments: vi.fn(() => paged()),
  };
  const inventory = {
    listInventoryItems: vi.fn(() => paged()),
    listInventoryReservations: vi.fn(() => paged()),
    listStockMovements: vi.fn(() => paged()),
    listWarehouses: vi.fn(() => paged()),
  };
  const sales = {
    createSalesOrder: vi.fn(() => of({ id: 'order-1', status: 'DRAFT' })),
    listSalesOrders: vi.fn(() => paged()),
  };
  const suppliers = {
    createSupplier: vi.fn(() => of({ id: 'supplier-1' })),
    listSuppliers: vi.fn(() => paged()),
  };
  const users = {
    createUser: vi.fn(() => of({ id: 'user-1' })),
    listUsers: vi.fn(() => paged()),
  };
  const optionsFacade = { loadOptions: vi.fn(() => of({})) };
  const router = { navigate: vi.fn(() => Promise.resolve(true)) };

  function createComponent(entity: string): ComponentFixture<EntityCreatePage> {
    TestBed.configureTestingModule({
      imports: [EntityCreatePage],
      providers: [
        provideHttpClient(),
        provideNoopAnimations(),
        { provide: Router, useValue: router },
        {
          provide: ActivatedRoute,
          useValue: { data: of({ entity }), snapshot: { data: { entity } } },
        },
        EntityRegistryService,
        { provide: ENTITY_CONFIGS, multi: true, useFactory: companiesEntityConfig },
        { provide: ENTITY_CONFIGS, multi: true, useFactory: salesOrdersEntityConfig },
        { provide: ENTITY_CONFIGS, multi: true, useFactory: categoriesEntityConfig },
        { provide: ENTITY_CONFIGS, multi: true, useFactory: productsEntityConfig },
        { provide: ENTITY_CONFIGS, multi: true, useFactory: usersEntityConfig },
        { provide: CepAddressService, useValue: cepAddress },
        { provide: CatalogService, useValue: catalog },
        { provide: CompaniesService, useValue: companies },
        { provide: ContactPersonsService, useValue: contactPersons },
        { provide: CustomersService, useValue: customers },
        { provide: FinancialService, useValue: financial },
        { provide: InventoryService, useValue: inventory },
        { provide: SalesService, useValue: sales },
        { provide: SuppliersService, useValue: suppliers },
        { provide: UsersService, useValue: users },
        { provide: EntityOptionsFacade, useValue: optionsFacade },
      ],
    });

    return TestBed.createComponent(EntityCreatePage);
  }

  beforeEach(() => {
    TestBed.resetTestingModule();
    vi.clearAllMocks();
  });

  it.each(['', '   '])('requests automatic SKU assignment for an empty entry (%j)', async (sku) => {
    const fixture = createComponent('products');
    const component = fixture.componentInstance;
    component.createForm().patchValue({
      sku,
      name: 'Product',
      amount: 10,
      currencyCode: 'BRL',
      categoryId: 'category-1',
    });
    await fixture.whenStable();
    expect(component.createForm().valid).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Leave blank to generate a SKU');
    component.submitCreate();
    expect(catalog.createProduct).toHaveBeenCalledWith({
      sku: undefined,
      name: 'Product',
      description: undefined,
      basePrice: { amount: 10, currencyCode: 'BRL' },
      categoryId: 'category-1',
    });
  });

  it('keeps manual SKUs and prevents overlong values', () => {
    const component = createComponent('products').componentInstance;
    component.createForm().patchValue({
      sku: ' custom-1 ',
      name: 'Product',
      amount: 10,
      currencyCode: 'BRL',
      categoryId: 'category-1',
    });
    component.submitCreate();
    expect(catalog.createProduct).toHaveBeenCalledWith(
      expect.objectContaining({ sku: 'custom-1' }),
    );
    catalog.createProduct.mockClear();
    component.createForm().controls['sku'].setValue('X'.repeat(80));
    expect(component.createForm().controls['sku'].valid).toBe(true);
    component.createForm().controls['sku'].setValue('X'.repeat(81));
    component.submitCreate();
    expect(component.createForm().controls['sku'].hasError('maxlength')).toBe(true);
    expect(catalog.createProduct).not.toHaveBeenCalled();
  });

  it('loads only lookup options needed by the current create page', () => {
    createComponent('sales-orders');

    expect(optionsFacade.loadOptions).toHaveBeenCalledWith(['customers', 'products', 'warehouses']);
  });

  it('loads delivery route details when a sales customer is selected', () => {
    const fixture = createComponent('sales-orders');
    const component = fixture.componentInstance;

    component.createForm().patchValue({ customerId: 'customer-1' });

    expect(customers.getCustomerById).toHaveBeenCalledWith('customer-1');
    expect(companies.getCompanyAddress).toHaveBeenCalledWith('company-1');
    expect(component.createForm().controls['deliveryRouteName'].value).toBe('ACME delivery');
    expect(component.createForm().controls['deliveryRouteDestinationDescription'].value).toBe(
      'ACME address',
    );
  });

  it('maps interactive sales order product rows into the API item array', () => {
    const fixture = createComponent('sales-orders');
    const component = fixture.componentInstance;

    component.createForm().setValue({
      customerId: 'customer-1',
      paymentMethod: 'CASH',
      fulfillmentType: 'PICKUP',
      deliveryRouteName: '',
      deliveryRouteDestinationDescription: '',
    });
    component.salesOrderItems().at(0).setValue({
      productId: 'product-1',
      warehouseId: 'warehouse-1',
      quantity: 3,
    });
    component.addSalesOrderItem();
    component.salesOrderItems().at(1).setValue({
      productId: 'product-2',
      warehouseId: 'warehouse-2',
      quantity: 7,
    });

    component.submitCreate();

    expect(sales.createSalesOrder).toHaveBeenCalledWith({
      saleType: 'B2C',
      paymentMethod: 'CASH',
      fulfillmentType: 'PICKUP',
      customerId: 'customer-1',
      deliveryRoute: undefined,
      items: [
        { productId: 'product-1', warehouseId: 'warehouse-1', quantity: 3 },
        { productId: 'product-2', warehouseId: 'warehouse-2', quantity: 7 },
      ],
    });
    expect(router.navigate).toHaveBeenCalledWith(['../'], { relativeTo: expect.any(Object) });
  });

  it('removes sales order product rows while keeping one required row', () => {
    const fixture = createComponent('sales-orders');
    const component = fixture.componentInstance;

    component.addSalesOrderItem();
    component.addSalesOrderItem();
    component.removeSalesOrderItem(1);
    component.removeSalesOrderItem(0);
    component.removeSalesOrderItem(0);

    expect(component.salesOrderItems().length).toBe(1);
  });

  it('blocks sales order creation when a product row is invalid', () => {
    const fixture = createComponent('sales-orders');
    const component = fixture.componentInstance;

    component.createForm().setValue({
      customerId: 'customer-1',
      paymentMethod: 'CASH',
      fulfillmentType: 'PICKUP',
      deliveryRouteName: '',
      deliveryRouteDestinationDescription: '',
    });
    component.salesOrderItems().at(0).patchValue({ productId: 'product-1' });
    component.submitCreate();

    expect(sales.createSalesOrder).not.toHaveBeenCalled();
  });

  it('requires and maps delivery route details for delivery sales orders', () => {
    const fixture = createComponent('sales-orders');
    const component = fixture.componentInstance;

    component.createForm().setValue({
      customerId: 'customer-1',
      paymentMethod: 'PIX',
      fulfillmentType: 'DELIVERY',
      deliveryRouteName: '',
      deliveryRouteDestinationDescription: '',
    });
    component.salesOrderItems().at(0).setValue({
      productId: 'product-1',
      warehouseId: 'warehouse-1',
      quantity: 3,
    });

    component.submitCreate();

    expect(sales.createSalesOrder).not.toHaveBeenCalled();

    component.createForm().patchValue({
      deliveryRouteName: 'Route A',
      deliveryRouteDestinationDescription: 'Customer loading dock',
    });
    component.submitCreate();

    expect(sales.createSalesOrder).toHaveBeenCalledWith({
      saleType: 'B2C',
      paymentMethod: 'PIX',
      fulfillmentType: 'DELIVERY',
      customerId: 'customer-1',
      deliveryRoute: {
        routeName: 'Route A',
        originWarehouseId: 'warehouse-1',
        destinationDescription: 'Customer loading dock',
      },
      items: [{ productId: 'product-1', warehouseId: 'warehouse-1', quantity: 3 }],
    });
  });

  it('does not block non-sales creation with unused sales order item controls', () => {
    const fixture = createComponent('companies');
    const component = fixture.componentInstance;

    component.createForm().setValue({
      legalName: 'ACME Ltd',
      tradeName: 'ACME',
      cnpj: '00.000.000/0001-00',
      type: 'FACTORY',
      postalCode: '01001-000',
      email: 'acme@example.com',
      phone: '',
      street: 'Praca da Se',
      number: '1',
      district: 'Se',
      city: 'Sao Paulo',
      region: 'SP',
      country: 'Brazil',
    });

    component.submitCreate();

    expect(companies.createCompany).toHaveBeenCalledWith({
      legalName: 'ACME Ltd',
      tradeName: 'ACME',
      cnpj: '00.000.000/0001-00',
      type: 'FACTORY',
      contact: { email: 'acme@example.com', phone: undefined },
      address: {
        street: 'Praca da Se',
        number: '1',
        district: 'Se',
        city: 'Sao Paulo',
        region: 'SP',
        country: 'Brazil',
        postalCode: '01001-000',
      },
    });
  });

  it('fills address fields from a CEP lookup on page-based forms', () => {
    const fixture = createComponent('companies');
    const component = fixture.componentInstance;

    component.createForm().patchValue({ postalCode: '01001-000' });
    component.lookupPostalCode();

    expect(cepAddress.lookup).toHaveBeenCalledWith('01001000');
    expect(component.createForm().getRawValue()).toEqual(
      expect.objectContaining({
        postalCode: '01001-000',
        street: 'Praca da Se',
        district: 'Se',
        city: 'Sao Paulo',
        region: 'SP',
        country: 'Brazil',
      }),
    );
  });

  it('routes non-page create resources back to the list', () => {
    const fixture = createComponent('categories');
    const component = fixture.componentInstance;

    fixture.detectChanges();

    expect(component.config().createMode).toBeUndefined();
    expect(router.navigate).toHaveBeenCalledWith(['../'], { relativeTo: expect.any(Object) });
  });
});
