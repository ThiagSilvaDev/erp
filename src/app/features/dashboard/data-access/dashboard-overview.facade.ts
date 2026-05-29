import { Injectable, inject } from '@angular/core';
import { catchError, forkJoin, map, of } from 'rxjs';

import { CatalogService } from '@generated-api/modules/catalog/Catalog.service';
import { CompaniesService } from '@generated-api/modules/companies/Companies.service';
import { ContactPersonsService } from '@generated-api/modules/contact-persons/ContactPersons.service';
import { CustomersService } from '@generated-api/modules/customers/Customers.service';
import { SuppliersService } from '@generated-api/modules/suppliers/Suppliers.service';
import { UsersService } from '@generated-api/modules/users/Users.service';
import { DashboardViewModel } from '../models/dashboard-view-model';

interface PageLike {
  content?: object[];
  page?: {
    totalElements?: number;
  };
  totalElements?: number;
}

function total(page: PageLike): number {
  return page.totalElements ?? page.page?.totalElements ?? 0;
}

function valueOf(record: object, key: string): string {
  if (key in record) {
    const value = (record as Record<string, unknown>)[key];
    if (typeof value === 'string' || typeof value === 'number') {
      return String(value);
    }
  }
  return '';
}

function nestedValueOf(record: object, key: string, childKey: string): string {
  if (key in record) {
    const value = (record as Record<string, unknown>)[key];
    if (value && typeof value === 'object') {
      return valueOf(value, childKey);
    }
  }
  return '';
}

@Injectable({ providedIn: 'root' })
export class DashboardOverviewFacade {
  private readonly catalog = inject(CatalogService);
  private readonly companies = inject(CompaniesService);
  private readonly contactPersons = inject(ContactPersonsService);
  private readonly customers = inject(CustomersService);
  private readonly suppliers = inject(SuppliersService);
  private readonly users = inject(UsersService);

  loadOverview() {
    const pageParams = { page: 0, size: 5 };

    return forkJoin({
      companies: this.companies
        .listCompanies(pageParams)
        .pipe(catchError(() => of({ content: [], totalElements: 0 }))),
      users: this.users
        .listUsers(pageParams)
        .pipe(catchError(() => of({ content: [], totalElements: 0 }))),
      products: this.catalog
        .listProducts(pageParams)
        .pipe(catchError(() => of({ content: [], totalElements: 0 }))),
      customers: this.customers
        .listCustomers(pageParams)
        .pipe(catchError(() => of({ content: [], totalElements: 0 }))),
      suppliers: this.suppliers
        .listSuppliers(pageParams)
        .pipe(catchError(() => of({ content: [], totalElements: 0 }))),
      contacts: this.contactPersons
        .listContactPersons(pageParams)
        .pipe(catchError(() => of({ content: [], totalElements: 0 }))),
    }).pipe(
      map(
        (result): DashboardViewModel => ({
          cards: [
            {
              label: 'Companies',
              value: total(result.companies),
              path: '/companies',
              icon: 'apartment',
            },
            {
              label: 'Users',
              value: total(result.users),
              path: '/users',
              icon: 'team',
            },
            {
              label: 'Products',
              value: total(result.products),
              path: '/products',
              icon: 'shop',
            },
            {
              label: 'Customers',
              value: total(result.customers),
              path: '/customers',
              icon: 'user',
            },
            {
              label: 'Suppliers',
              value: total(result.suppliers),
              path: '/suppliers',
              icon: 'contacts',
            },
            {
              label: 'Contact persons',
              value: total(result.contacts),
              path: '/contact-persons',
              icon: 'team',
            },
          ],
          recentRecords: [
            ...(result.companies.content ?? []).map((item) => ({
              module: 'Companies',
              name: valueOf(item, 'tradeName') || valueOf(item, 'legalName') || 'Company',
              detail: nestedValueOf(item, 'cnpj', 'number') || valueOf(item, 'status'),
              path: '/companies',
            })),
            ...(result.products.content ?? []).map((item) => ({
              module: 'Products',
              name: valueOf(item, 'name') || 'Product',
              detail: valueOf(item, 'sku') || valueOf(item, 'status'),
              path: '/products',
            })),
            ...(result.customers.content ?? []).map((item) => ({
              module: 'Customers',
              name: valueOf(item, 'name') || 'Customer',
              detail: valueOf(item, 'document'),
              path: '/customers',
            })),
          ].slice(0, 8),
        }),
      ),
    );
  }
}
