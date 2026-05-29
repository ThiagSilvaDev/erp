import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { CatalogService } from '@generated-api/modules/catalog/Catalog.service';
import { CompaniesService } from '@generated-api/modules/companies/Companies.service';
import { CustomersService } from '@generated-api/modules/customers/Customers.service';
import { FinancialService } from '@generated-api/modules/financial/Financial.service';
import { InventoryService } from '@generated-api/modules/inventory/Inventory.service';
import { SalesService } from '@generated-api/modules/sales/Sales.service';
import { ProductsOptionLoader, CustomersOptionLoader } from './entity-option-loaders';
import { EntitySelectOptions } from '../models/entity-view-model';
import { ENTITY_OPTION_LOADERS } from '../tokens/entity.tokens';
import { EntityOptionsFacade } from './entity-options.facade';

function paged(content: object[] = []) {
  return of({ content, page: { totalElements: content.length } });
}

describe('EntityOptionsFacade', () => {
  const catalog = {
    listCategories: vi.fn(() => paged([{ id: 'category-1', name: 'Parts' }])),
    listProducts: vi.fn(() => paged([{ id: 'product-1', name: 'Widget' }])),
  };
  const companies = {
    listCompanies: vi.fn(() => paged([{ id: 'company-1', tradeName: 'ACME' }])),
  };
  const customers = {
    listCustomers: vi.fn(() => paged([{ id: 'customer-1', name: 'Customer One' }])),
  };
  const financial = {
    listInvoices: vi.fn(() => paged([{ id: 'invoice-1', status: 'OPEN' }])),
  };
  const inventory = {
    listWarehouses: vi.fn(() => paged([{ id: 'warehouse-1', name: 'Main' }])),
  };
  const sales = {
    listSalesOrders: vi.fn(() => paged([{ id: 'order-1', status: 'DRAFT' }])),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        EntityOptionsFacade,
        { provide: ENTITY_OPTION_LOADERS, multi: true, useClass: CustomersOptionLoader },
        { provide: ENTITY_OPTION_LOADERS, multi: true, useClass: ProductsOptionLoader },
        { provide: CatalogService, useValue: catalog },
        { provide: CompaniesService, useValue: companies },
        { provide: CustomersService, useValue: customers },
        { provide: FinancialService, useValue: financial },
        { provide: InventoryService, useValue: inventory },
        { provide: SalesService, useValue: sales },
      ],
    });

    vi.clearAllMocks();
  });

  it('loads only the requested option groups', () => {
    const facade = TestBed.inject(EntityOptionsFacade);
    let options = {};

    facade.loadOptions(['customers', 'products']).subscribe((value) => {
      options = value;
    });

    expect(options).toEqual({
      customers: [{ value: 'customer-1', label: 'Customer One' }],
      products: [{ value: 'product-1', label: 'Widget' }],
    });
    expect(customers.listCustomers).toHaveBeenCalledTimes(1);
    expect(catalog.listProducts).toHaveBeenCalledTimes(1);
    expect(companies.listCompanies).not.toHaveBeenCalled();
    expect(catalog.listCategories).not.toHaveBeenCalled();
    expect(financial.listInvoices).not.toHaveBeenCalled();
    expect(inventory.listWarehouses).not.toHaveBeenCalled();
    expect(sales.listSalesOrders).not.toHaveBeenCalled();
  });

  it('dedupes requested option groups', () => {
    const facade = TestBed.inject(EntityOptionsFacade);

    facade.loadOptions(['customers', 'customers']).subscribe();

    expect(customers.listCustomers).toHaveBeenCalledTimes(1);
  });

  it('does not call lookup APIs when no option groups are requested', () => {
    const facade = TestBed.inject(EntityOptionsFacade);
    let options: EntitySelectOptions = { customers: [] };

    facade.loadOptions([]).subscribe((value) => {
      options = value;
    });

    expect(options).toEqual({});
    expect(companies.listCompanies).not.toHaveBeenCalled();
    expect(catalog.listCategories).not.toHaveBeenCalled();
    expect(catalog.listProducts).not.toHaveBeenCalled();
    expect(customers.listCustomers).not.toHaveBeenCalled();
    expect(financial.listInvoices).not.toHaveBeenCalled();
    expect(inventory.listWarehouses).not.toHaveBeenCalled();
    expect(sales.listSalesOrders).not.toHaveBeenCalled();
  });
});
