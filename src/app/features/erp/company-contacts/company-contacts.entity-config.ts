import { inject } from '@angular/core';
import { map } from 'rxjs';

import { CompaniesService } from '@generated-api/modules/companies/Companies.service';
import type { CompanyContactResponse, CompanyResponse } from '@generated-api/modules/companies/dtos';
import { EntityConfig, EntityRecord } from '../entity/models/entity-view-model';
import { provideEntityFeature } from '../entity/providers/entity-feature.provider';
import { text } from '../entity/utils/entity-utils';

interface CompanyContactRecord extends CompanyContactResponse {
  companyName?: string;
}

function companyName(company: CompanyResponse): string {
  return company.tradeName || company.legalName || text(company.id) || 'Company';
}

function companyContactRecord(company: CompanyResponse): CompanyContactRecord | null {
  if (!company.primaryContact) {
    return null;
  }

  return {
    ...company.primaryContact,
    id: text(company.id),
    companyId: text(company.id),
    companyName: companyName(company),
  };
}

function pageDataFromCompanies(response: { content?: CompanyResponse[] }): {
  content: EntityRecord[];
  totalElements: number;
} {
  const content = (response.content ?? [])
    .map((company) => companyContactRecord(company))
    .filter((record): record is CompanyContactRecord => Boolean(record));

  return {
    content,
    totalElements: content.length,
  };
}

export function companyContactsEntityConfig(): EntityConfig {
  const companies = inject(CompaniesService);
  return {
    key: 'company-contacts',
    title: 'Company contacts',
    description: 'Primary contacts attached to company records.',
    readonly: true,
    columns: [
      { key: 'name', label: 'Name' },
      { key: 'companyName', label: 'Company' },
      { key: 'role', label: 'Role' },
      { key: 'contact.email', label: 'Email' },
    ],
    detailFields: [
      { key: 'name', label: 'Name' },
      { key: 'companyName', label: 'Company' },
      { key: 'companyId', label: 'Company ID' },
      { key: 'role', label: 'Role' },
      { key: 'contact.email', label: 'Email' },
      { key: 'contact.phone', label: 'Phone' },
      { key: 'primaryContact', label: 'Primary contact' },
    ],
    searchFields: [],
    createFields: [],
    list: (_criteria, page, size) =>
      companies.listCompanies({ page, size }).pipe(map(pageDataFromCompanies)),
    getById: (id) =>
      companies.getCompanyById(id).pipe(
        map((company) => companyContactRecord(company) ?? ({ id } satisfies EntityRecord)),
      ),
  };
}

export const provideCompanyContactsEntityConfig = () =>
  provideEntityFeature({ config: companyContactsEntityConfig, optionLoaders: [] });
