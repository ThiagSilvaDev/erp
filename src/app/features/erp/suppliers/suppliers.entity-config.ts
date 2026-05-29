import { inject } from '@angular/core';
import { map } from 'rxjs';

import { SuppliersService } from '@generated-api/modules/suppliers/Suppliers.service';
import {
  partyFields,
  partyPayload,
  partySearch,
} from '../entity/config/party-entity-fields';
import { CompaniesOptionLoader } from '../entity/data-access/entity-option-loaders';
import { EntityConfig } from '../entity/models/entity-view-model';
import { provideEntityFeature } from '../entity/providers/entity-feature.provider';
import { pageData } from '../entity/utils/entity-utils';

export function suppliersEntityConfig(): EntityConfig {
  const suppliers = inject(SuppliersService);
  return {
    key: 'suppliers',
    title: 'Suppliers',
    description: 'Supplier accounts.',
    createMode: 'page',
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
      suppliers.listSuppliers({ ...criteria, page, size }).pipe(map(pageData)),
    create: (value) => suppliers.createSupplier(partyPayload(value)),
    getById: (id) => suppliers.getSupplierById(id),
  };
}

export const provideSuppliersEntityConfig = () =>
  provideEntityFeature({ config: suppliersEntityConfig, optionLoaders: [CompaniesOptionLoader] });
