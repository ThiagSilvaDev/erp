import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { provideRouter, Router } from '@angular/router';
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
import { categoriesEntityConfig } from '../../../catalog/catalog.entity-config';
import { companiesEntityConfig } from '../../../companies/companies.entity-config';
import { inventoryItemsEntityConfig } from '../../../inventory/inventory.entity-config';
import { salesOrdersEntityConfig } from '../../../sales/sales.entity-config';
import { CepAddressService } from '../../data-access/cep-address.service';
import { EntityOptionsFacade } from '../../data-access/entity-options.facade';
import { EntityRegistryService } from '../../data-access/entity-registry.service';
import { ENTITY_CONFIGS } from '../../tokens/entity.tokens';
import { EntityPage } from './entity-page';

function paged(content: object[] = []) {
  return of({ content, page: { totalElements: content.length } });
}

describe('EntityPage', () => {
  const catalog = {
    listCategories: vi.fn(() => paged()),
    listProducts: vi.fn(() => paged()),
  };
  const companies = {
    getCompanyById: vi.fn((id: string) => of({ id, legalName: 'ACME LTDA', tradeName: 'ACME' })),
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
  const customers = { listCustomers: vi.fn(() => paged()) };
  const financial = {
    listLedgerEntries: vi.fn(() => paged()),
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
    getSalesOrderById: vi.fn(() => of({ id: 'order-1', status: 'DRAFT' })),
    listSalesOrders: vi.fn(() => paged()),
  };
  const suppliers = { listSuppliers: vi.fn(() => paged()) };
  const users = { listUsers: vi.fn(() => paged()) };
  const optionsFacade = { loadOptions: vi.fn(() => of({})) };

  function createComponent(entity: string): ComponentFixture<EntityPage> {
    TestBed.configureTestingModule({
      imports: [EntityPage],
      providers: [
        provideHttpClient(),
        provideNoopAnimations(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { data: of({ entity }), snapshot: { data: { entity } } },
        },
        EntityRegistryService,
        { provide: ENTITY_CONFIGS, multi: true, useFactory: companiesEntityConfig },
        { provide: ENTITY_CONFIGS, multi: true, useFactory: salesOrdersEntityConfig },
        { provide: ENTITY_CONFIGS, multi: true, useFactory: inventoryItemsEntityConfig },
        { provide: ENTITY_CONFIGS, multi: true, useFactory: categoriesEntityConfig },
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

    return TestBed.createComponent(EntityPage);
  }

  beforeEach(() => {
    TestBed.resetTestingModule();
    vi.clearAllMocks();
  });

  it('loads only lookup options needed by the current list page', () => {
    createComponent('sales-orders');

    expect(optionsFacade.loadOptions).toHaveBeenCalledWith(['customers']);
  });

  it('fills address fields from a CEP lookup', () => {
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

  it('maps sales order create fields into the API item array', () => {
    const fixture = createComponent('sales-orders');
    vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    const component = fixture.componentInstance;

    component.createForm().setValue({
      customerId: 'customer-1',
      paymentMethod: 'CASH',
      fulfillmentType: 'PICKUP',
      deliveryRouteName: '',
      deliveryRouteDestinationDescription: '',
      productId: 'product-1',
      warehouseId: 'warehouse-1',
      quantity: 3,
    });

    component.submitCreate();

    expect(sales.createSalesOrder).toHaveBeenCalledWith({
      saleType: 'B2C',
      paymentMethod: 'CASH',
      fulfillmentType: 'PICKUP',
      customerId: 'customer-1',
      deliveryRoute: undefined,
      items: [{ productId: 'product-1', warehouseId: 'warehouse-1', quantity: 3 }],
    });
  });

  it('keeps inventory items read-only', () => {
    const fixture = createComponent('inventory-items');
    const component = fixture.componentInstance;

    component.openCreate();

    expect(component.config().readonly).toBe(true);
    expect(component.isCreateOpen()).toBe(false);
    expect(inventory.listInventoryItems).toHaveBeenCalled();
  });

  it('opens the create modal for simple resources', () => {
    const fixture = createComponent('categories');
    const component = fixture.componentInstance;

    component.openCreate();

    expect(component.config().createMode).toBeUndefined();
    expect(component.isCreateOpen()).toBe(true);
  });

  it('routes complex resources to the create page', () => {
    const fixture = createComponent('sales-orders');
    const component = fixture.componentInstance;
    const router = TestBed.inject(Router);
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    component.openCreate();

    expect(component.config().createMode).toBe('page');
    expect(component.isCreateOpen()).toBe(false);
    expect(navigate).toHaveBeenCalledWith(['new'], { relativeTo: expect.any(Object) });
  });

  it('opens the detail modal with a record that has no id', () => {
    const fixture = createComponent('companies');
    const component = fixture.componentInstance;
    const record = { legalName: 'ACME LTDA' };

    component.openDetail(record);

    expect(component.selectedRecord()).toBe(record);
    expect(component.isDetailOpen()).toBe(true);
    expect(companies.getCompanyById).not.toHaveBeenCalled();
  });

  it('loads the latest detail before opening the detail modal for id-backed records', () => {
    const fixture = createComponent('companies');
    const component = fixture.componentInstance;

    component.openDetail({ id: 'company-1', legalName: 'Stale name' });

    expect(companies.getCompanyById).toHaveBeenCalledWith('company-1');
    expect(component.selectedRecord()).toEqual({
      id: 'company-1',
      legalName: 'ACME LTDA',
      tradeName: 'ACME',
    });
    expect(component.isDetailOpen()).toBe(true);
  });
});
