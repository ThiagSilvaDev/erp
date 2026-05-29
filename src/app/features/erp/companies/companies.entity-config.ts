import { inject } from '@angular/core';
import { map, of } from 'rxjs';

import { CompaniesService } from '@generated-api/modules/companies/Companies.service';
import { activeStatusOptions, companyTypeOptions } from '../entity/config/entity-field-options';
import { EntityConfig, EntityRecord } from '../entity/models/entity-view-model';
import { provideEntityFeature } from '../entity/providers/entity-feature.provider';
import { getValue, pageData, text } from '../entity/utils/entity-utils';

function hasValue(record: EntityRecord, key: string): boolean {
  return getValue(record, key) !== '-';
}

function companyAddress(record: EntityRecord): string {
  return [
    getValue(record, 'address.street'),
    getValue(record, 'address.number'),
    getValue(record, 'address.district'),
    getValue(record, 'address.city'),
    getValue(record, 'address.region'),
    getValue(record, 'address.country'),
    getValue(record, 'address.postalCode'),
  ]
    .filter((part) => part !== '-')
    .join(', ');
}

function openExternalLink(url: string): void {
  window.open(url, '_blank', 'noopener,noreferrer');
}

function openMailTo(email: string): void {
  window.location.href = `mailto:${email}`;
}

export function companiesEntityConfig(): EntityConfig {
  const companies = inject(CompaniesService);
  return {
    key: 'companies',
    title: 'Companies',
    description: 'Legal entities and operating companies.',
    createMode: 'page',
    columns: [
      { key: 'tradeName', label: 'Trade name' },
      { key: 'legalName', label: 'Legal name' },
      { key: 'cnpj.formattedNumber', label: 'CNPJ' },
      { key: 'type', label: 'Type' },
      { key: 'status', label: 'Status' },
    ],
    detailFields: [
      { key: 'legalName', label: 'Legal name' },
      { key: 'tradeName', label: 'Trade name' },
      { key: 'cnpj.formattedNumber', label: 'CNPJ' },
      { key: 'type', label: 'Type' },
      { key: 'status', label: 'Status' },
      { key: 'contact.email', label: 'Email' },
      { key: 'contact.phone', label: 'Phone' },
      { key: 'address.city', label: 'City' },
      { key: 'address.country', label: 'Country' },
    ],
    searchFields: [
      { key: 'name', label: 'Name' },
      { key: 'cnpj', label: 'CNPJ' },
      { key: 'type', label: 'Type', type: 'select', options: companyTypeOptions },
      { key: 'status', label: 'Status', type: 'select', options: activeStatusOptions },
    ],
    createFields: [
      { key: 'legalName', label: 'Legal name', required: true, section: 'Identity' },
      { key: 'tradeName', label: 'Trade name', section: 'Identity' },
      { key: 'cnpj', label: 'CNPJ', required: true, section: 'Identity' },
      {
        key: 'type',
        label: 'Type',
        required: true,
        section: 'Identity',
        type: 'select',
        options: companyTypeOptions,
      },
      { key: 'postalCode', label: 'Postal code', required: true, section: 'Address' },
      { key: 'email', label: 'Email', type: 'email', section: 'Contact' },
      { key: 'phone', label: 'Phone', section: 'Contact' },
      { key: 'street', label: 'Street', required: true, section: 'Address' },
      { key: 'number', label: 'Number', required: true, section: 'Address' },
      { key: 'district', label: 'District', required: true, section: 'Address' },
      { key: 'city', label: 'City', required: true, section: 'Address' },
      { key: 'region', label: 'Region', required: true, section: 'Address' },
      { key: 'country', label: 'Country', required: true, section: 'Address' }
    ],
    list: (criteria, page, size) =>
      companies.listCompanies({ ...criteria, page, size }).pipe(map(pageData)),
    create: (value) =>
      companies.createCompany({
        legalName: text(value['legalName']),
        tradeName: text(value['tradeName']) || undefined,
        cnpj: text(value['cnpj']),
        type: text(value['type']) === 'CORRESPONDENT' ? 'CORRESPONDENT' : 'FACTORY',
        contact: text(value['email'])
          ? { email: text(value['email']), phone: text(value['phone']) || undefined }
          : undefined,
        address: {
          street: text(value['street']),
          number: text(value['number']),
          district: text(value['district']),
          city: text(value['city']),
          region: text(value['region']),
          country: text(value['country']),
          postalCode: text(value['postalCode']),
        },
      }),
    getById: (id) => companies.getCompanyById(id),
    actions: [
      {
        label: 'Map',
        icon: 'environment',
        isVisible: (record) => Boolean(companyAddress(record)),
        refreshAfterRun: false,
        successMessage: null,
        run: (record) => {
          openExternalLink(
            `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(
              companyAddress(record),
            )}`,
          );
          return of(record);
        },
      },
      {
        label: 'Email',
        icon: 'mail',
        isVisible: (record) => hasValue(record, 'contact.email'),
        refreshAfterRun: false,
        successMessage: null,
        run: (record) => {
          openMailTo(getValue(record, 'contact.email'));
          return of(record);
        },
      },
    ],
  };
}

export const provideCompaniesEntityConfig = () =>
  provideEntityFeature({ config: companiesEntityConfig, optionLoaders: [] });
