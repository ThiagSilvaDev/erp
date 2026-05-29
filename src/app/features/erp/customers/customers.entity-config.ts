import { inject } from '@angular/core';
import { map } from 'rxjs';

import { CustomersService } from '@generated-api/modules/customers/Customers.service';
import { partyFields, partyPayload, partySearch } from '../entity/config/party-entity-fields';
import { CompaniesOptionLoader } from '../entity/data-access/entity-option-loaders';
import { EntityConfig } from '../entity/models/entity-view-model';
import { provideEntityFeature } from '../entity/providers/entity-feature.provider';
import { pageData, text } from '../entity/utils/entity-utils';

export function customersEntityConfig(): EntityConfig {
  const customers = inject(CustomersService);
  return {
    key: 'customers',
    title: 'Customers',
    description: 'Customer accounts.',
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
      customers.listCustomers({ ...criteria, page, size }).pipe(map(pageData)),
    create: (value) => {
      const payload = partyPayload(value);
      return customers.createCustomer({
        name: payload.name,
        document: payload.document,
        ownerCompanyId: text(value['companyId']),
        contact: payload.contact,
        address: payload.address,
      });
    },
    getById: (id) => customers.getCustomerById(id),
  };
}

export const provideCustomersEntityConfig = () =>
  provideEntityFeature({ config: customersEntityConfig, optionLoaders: [CompaniesOptionLoader] });
