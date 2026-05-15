import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { forkJoin, map, of, catchError } from 'rxjs';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzTableModule } from 'ng-zorro-antd/table';

import { CatalogService } from '../../api/modules/catalog/catalog.service';
import { CompaniesService } from '../../api/modules/companies/companies.service';
import { ContactPersonsService } from '../../api/modules/contact-persons/contact-persons.service';
import { CustomersService } from '../../api/modules/customers/customers.service';
import { SuppliersService } from '../../api/modules/suppliers/suppliers.service';
import { UsersService } from '../../api/modules/users/users.service';

interface SummaryCard {
  label: string;
  value: number;
  path: string;
  icon: string;
}

interface RecentRecord {
  module: string;
  name: string;
  detail: string;
  path: string;
}

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, NzCardModule, NzIconModule, NzTableModule],
  templateUrl: './dashboard.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Dashboard {
  private readonly catalog = inject(CatalogService);
  private readonly companies = inject(CompaniesService);
  private readonly contactPersons = inject(ContactPersonsService);
  private readonly customers = inject(CustomersService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly suppliers = inject(SuppliersService);
  private readonly users = inject(UsersService);

  readonly isLoading = signal(true);
  readonly cards = signal<SummaryCard[]>([]);
  readonly recentRecords = signal<RecentRecord[]>([]);

  constructor() {
    const pageable = { page: 0, size: 5 };

    forkJoin({
      companies: this.companies
        .listCompanies({ criteria: {}, pageable })
        .pipe(catchError(() => of({ content: [], totalElements: 0 }))),
      users: this.users
        .listUsers({ criteria: {}, pageable })
        .pipe(catchError(() => of({ content: [], totalElements: 0 }))),
      products: this.catalog
        .listProducts({ criteria: {}, pageable })
        .pipe(catchError(() => of({ content: [], totalElements: 0 }))),
      customers: this.customers
        .listCustomers({ criteria: {}, pageable })
        .pipe(catchError(() => of({ content: [], totalElements: 0 }))),
      suppliers: this.suppliers
        .listSuppliers({ criteria: {}, pageable })
        .pipe(catchError(() => of({ content: [], totalElements: 0 }))),
      contacts: this.contactPersons
        .listContactPersons({ pageable })
        .pipe(catchError(() => of({ content: [], totalElements: 0 }))),
    })
      .pipe(
        map((result) => ({
          cards: [
            {
              label: 'Companies',
              value: result.companies.totalElements ?? 0,
              path: '/companies',
              icon: 'apartment',
            },
            { label: 'Users', value: result.users.totalElements ?? 0, path: '/users', icon: 'team' },
            {
              label: 'Products',
              value: result.products.totalElements ?? 0,
              path: '/products',
              icon: 'shop',
            },
            {
              label: 'Customers',
              value: result.customers.totalElements ?? 0,
              path: '/customers',
              icon: 'user',
            },
            {
              label: 'Suppliers',
              value: result.suppliers.totalElements ?? 0,
              path: '/suppliers',
              icon: 'contacts',
            },
            {
              label: 'Contact persons',
              value: result.contacts.totalElements ?? 0,
              path: '/contact-persons',
              icon: 'team',
            },
          ],
          recent: [
            ...(result.companies.content ?? []).map((item) => ({
              module: 'Companies',
              name: item.tradeName || item.legalName || 'Company',
              detail: item.cnpj?.number ?? item.status ?? '',
              path: '/companies',
            })),
            ...(result.products.content ?? []).map((item) => ({
              module: 'Products',
              name: item.name ?? 'Product',
              detail: item.sku ?? item.status ?? '',
              path: '/products',
            })),
            ...(result.customers.content ?? []).map((item) => ({
              module: 'Customers',
              name: item.name ?? 'Customer',
              detail: item.document ?? '',
              path: '/customers',
            })),
          ].slice(0, 8),
        })),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(({ cards, recent }) => {
        this.cards.set(cards);
        this.recentRecords.set(recent);
        this.isLoading.set(false);
      });
  }
}
