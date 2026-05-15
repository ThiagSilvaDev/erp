import { Injectable, inject } from '@angular/core';
import { catchError, forkJoin, map, of } from 'rxjs';

import { CatalogService } from '../../../api/modules/catalog/catalog.service';
import { CompaniesService } from '../../../api/modules/companies/companies.service';
import { CustomersService } from '../../../api/modules/customers/customers.service';
import { EntitySelectOptions } from '../models/entity-view-model';

@Injectable({ providedIn: 'root' })
export class EntityOptionsFacade {
  private readonly catalog = inject(CatalogService);
  private readonly companies = inject(CompaniesService);
  private readonly customers = inject(CustomersService);

  loadOptions() {
    const pageable = { page: 0, size: 50 };

    return forkJoin({
      companies: this.companies.listCompanies({ criteria: {}, pageable }).pipe(
        map((page) =>
          (page.content ?? []).map((item) => ({
            value: item.id ?? '',
            label: item.tradeName || item.legalName || item.id || 'Company',
          })),
        ),
        catchError(() => of([])),
      ),
      categories: this.catalog.listCategories({ criteria: {}, pageable }).pipe(
        map((page) =>
          (page.content ?? []).map((item) => ({
            value: item.id ?? '',
            label: item.name || item.code || item.id || 'Category',
          })),
        ),
        catchError(() => of([])),
      ),
      customers: this.customers.listCustomers({ criteria: {}, pageable }).pipe(
        map((page) =>
          (page.content ?? []).map((item) => ({
            value: item.id ?? '',
            label: item.name || item.document || item.id || 'Customer',
          })),
        ),
        catchError(() => of([])),
      ),
    }).pipe(map((options): EntitySelectOptions => options));
  }
}
