import { EntityCreateValue, EntityField } from '../models/entity-view-model';
import { text } from '../utils/entity-utils';

export const partySearch: EntityField[] = [
  { key: 'name', label: 'Name' },
  { key: 'document', label: 'Document' },
  { key: 'companyId', label: 'Company', type: 'select', optionsKey: 'companies' },
];

export const partyFields: EntityField[] = [
  { key: 'name', label: 'Name', required: true, section: 'Identity' },
  { key: 'document', label: 'Document', required: true, section: 'Identity' },
  {
    key: 'companyId',
    label: 'Company',
    required: true,
    section: 'Identity',
    type: 'select',
    optionsKey: 'companies',
  },
  { key: 'email', label: 'Email', type: 'email', section: 'Contact' },
  { key: 'phone', label: 'Phone', section: 'Contact' },
  { key: 'street', label: 'Street', section: 'Address' },
  { key: 'number', label: 'Number', section: 'Address' },
  { key: 'district', label: 'District', section: 'Address' },
  { key: 'city', label: 'City', section: 'Address' },
  { key: 'region', label: 'Region', section: 'Address' },
  { key: 'country', label: 'Country', section: 'Address' },
  { key: 'postalCode', label: 'Postal code', section: 'Address' },
];

export function partyPayload(value: EntityCreateValue) {
  return {
    name: text(value['name']),
    document: text(value['document']),
    companyId: text(value['companyId']),
    contact: text(value['email'])
      ? { email: text(value['email']), phone: text(value['phone']) || undefined }
      : undefined,
    address:
      text(value['street']) || text(value['city']) || text(value['country'])
        ? {
            postalCode: text(value['postalCode']),
            street: text(value['street']),
            number: text(value['number']),
            district: text(value['district']),
            city: text(value['city']),
            region: text(value['region']),
            country: text(value['country'])
          }
        : undefined,
  };
}
