import { inject } from '@angular/core';
import { map } from 'rxjs';

import { ContactPersonsService } from '@generated-api/modules/contact-persons/ContactPersons.service';
import { CustomersOptionLoader } from '../entity/data-access/entity-option-loaders';
import { EntityConfig } from '../entity/models/entity-view-model';
import { provideEntityFeature } from '../entity/providers/entity-feature.provider';
import { pageData, text } from '../entity/utils/entity-utils';

export function contactPersonsEntityConfig(): EntityConfig {
  const contactPersons = inject(ContactPersonsService);
  return {
    key: 'contact-persons',
    title: 'Contact persons',
    description: 'People attached to customer accounts.',
    columns: [
      { key: 'name', label: 'Name' },
      { key: 'customerId', label: 'Customer ID' },
      { key: 'contact.email', label: 'Email' },
    ],
    detailFields: [
      { key: 'name', label: 'Name' },
      { key: 'customerId', label: 'Customer ID' },
      { key: 'contact.email', label: 'Email' },
      { key: 'contact.phone', label: 'Phone' },
    ],
    searchFields: [],
    createFields: [
      { key: 'name', label: 'Name', required: true },
      {
        key: 'customerId',
        label: 'Customer',
        required: true,
        type: 'select',
        optionsKey: 'customers',
      },
      { key: 'email', label: 'Email', required: true, type: 'email' },
      { key: 'phone', label: 'Phone' },
    ],
    list: (_criteria, page, size) =>
      contactPersons.listContactPersons({ page, size }).pipe(map(pageData)),
    create: (value) =>
      contactPersons.createContactPerson({
        name: text(value['name']),
        customerId: text(value['customerId']),
        contact: { email: text(value['email']), phone: text(value['phone']) || undefined },
      }),
    getById: (id) => contactPersons.getContactPersonById(id),
  };
}

export const provideContactPersonsEntityConfig = () =>
  provideEntityFeature({
    config: contactPersonsEntityConfig,
    optionLoaders: [CustomersOptionLoader],
  });
