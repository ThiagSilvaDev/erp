import { Injectable, inject } from '@angular/core';
import { map } from 'rxjs';

import { CatalogService } from '@generated-api/modules/catalog/Catalog.service';
import { CompaniesService } from '@generated-api/modules/companies/Companies.service';
import { CustomersService } from '@generated-api/modules/customers/Customers.service';
import { FinancialService } from '@generated-api/modules/financial/Financial.service';
import { InventoryService } from '@generated-api/modules/inventory/Inventory.service';
import { SalesService } from '@generated-api/modules/sales/Sales.service';
import { EntityOptionLoader } from '../models/entity-view-model';

const pageParams = { page: 0, size: 50 };

function valueOf(record: object, key: string): string {
  if (key in record) {
    const value = (record as Record<string, unknown>)[key];
    if (typeof value === 'string' || typeof value === 'number') {
      return String(value);
    }
  }
  return '';
}

@Injectable()
export class CompaniesOptionLoader implements EntityOptionLoader {
  private readonly companies = inject(CompaniesService);
  readonly key = 'companies' as const;

  load() {
    return this.companies.listCompanies(pageParams).pipe(
      map((page) =>
        (page.content ?? []).map((item) => ({
          value: valueOf(item, 'id'),
          label:
            valueOf(item, 'tradeName') ||
            valueOf(item, 'legalName') ||
            valueOf(item, 'id') ||
            'Company',
        })),
      ),
    );
  }
}

@Injectable()
export class CategoriesOptionLoader implements EntityOptionLoader {
  private readonly catalog = inject(CatalogService);
  readonly key = 'categories' as const;

  load() {
    return this.catalog.listCategories(pageParams).pipe(
      map((page) =>
        (page.content ?? []).map((item) => ({
          value: valueOf(item, 'id'),
          label: valueOf(item, 'name') || valueOf(item, 'code') || valueOf(item, 'id') || 'Category',
        })),
      ),
    );
  }
}

@Injectable()
export class CustomersOptionLoader implements EntityOptionLoader {
  private readonly customers = inject(CustomersService);
  readonly key = 'customers' as const;

  load() {
    return this.customers.listCustomers(pageParams).pipe(
      map((page) =>
        (page.content ?? []).map((item) => ({
          value: valueOf(item, 'id'),
          label:
            valueOf(item, 'name') ||
            valueOf(item, 'document') ||
            valueOf(item, 'id') ||
            'Customer',
        })),
      ),
    );
  }
}

@Injectable()
export class ProductsOptionLoader implements EntityOptionLoader {
  private readonly catalog = inject(CatalogService);
  readonly key = 'products' as const;

  load() {
    return this.catalog.listProducts(pageParams).pipe(
      map((page) =>
        (page.content ?? []).map((item) => ({
          value: valueOf(item, 'id'),
          label: valueOf(item, 'name') || valueOf(item, 'sku') || valueOf(item, 'id') || 'Product',
        })),
      ),
    );
  }
}

@Injectable()
export class WarehousesOptionLoader implements EntityOptionLoader {
  private readonly inventory = inject(InventoryService);
  readonly key = 'warehouses' as const;

  load() {
    return this.inventory.listWarehouses(pageParams).pipe(
      map((page) =>
        (page.content ?? []).map((item) => ({
          value: valueOf(item, 'id'),
          label:
            valueOf(item, 'name') || valueOf(item, 'code') || valueOf(item, 'id') || 'Warehouse',
        })),
      ),
    );
  }
}

@Injectable()
export class InvoicesOptionLoader implements EntityOptionLoader {
  private readonly financial = inject(FinancialService);
  readonly key = 'invoices' as const;

  load() {
    return this.financial.listInvoices(pageParams).pipe(
      map((page) =>
        (page.content ?? []).map((item) => ({
          value: valueOf(item, 'id'),
          label: valueOf(item, 'id')
            ? `${valueOf(item, 'id')} - ${valueOf(item, 'status') || 'Invoice'}`
            : 'Invoice',
        })),
      ),
    );
  }
}

@Injectable()
export class SalesOrdersOptionLoader implements EntityOptionLoader {
  private readonly sales = inject(SalesService);
  readonly key = 'salesOrders' as const;

  load() {
    return this.sales.listSalesOrders(pageParams).pipe(
      map((page) =>
        (page.content ?? []).map((item) => ({
          value: valueOf(item, 'id'),
          label: valueOf(item, 'id')
            ? `${valueOf(item, 'id')} - ${valueOf(item, 'status') || 'Order'}`
            : 'Sales order',
        })),
      ),
    );
  }
}
