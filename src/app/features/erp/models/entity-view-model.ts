import { FormControl, FormGroup } from '@angular/forms';
import { Observable } from 'rxjs';

import { CategoryResponse, ProductResponse } from '../../../api/modules/catalog/dtos';
import { CompanyResponse } from '../../../api/modules/companies/dtos';
import { ContactPersonResponse } from '../../../api/modules/contact-persons/dtos';
import { CustomerResponse } from '../../../api/modules/customers/dtos';
import { SupplierResponse } from '../../../api/modules/suppliers/dtos';
import { UserResponse } from '../../../api/modules/users/dtos';

export type EntityKey =
  | 'categories'
  | 'companies'
  | 'contact-persons'
  | 'customers'
  | 'products'
  | 'suppliers'
  | 'users';

export type EntityRecord =
  | CategoryResponse
  | CompanyResponse
  | ContactPersonResponse
  | CustomerResponse
  | ProductResponse
  | SupplierResponse
  | UserResponse;

export type EntityFormValue = string | number;
export type EntityForm = FormGroup<Record<string, FormControl<EntityFormValue>>>;

export interface EntityPageData {
  content: EntityRecord[];
  totalElements: number;
}

export interface EntitySelectOption {
  label: string;
  value: string;
}

export interface EntityField {
  key: string;
  label: string;
  required?: boolean;
  type?: 'email' | 'number' | 'password' | 'select';
  optionsKey?: 'categories' | 'companies' | 'customers';
}

export interface EntityColumn {
  key: string;
  label: string;
}

export interface EntityConfig {
  key: EntityKey;
  title: string;
  description: string;
  columns: EntityColumn[];
  detailFields: EntityColumn[];
  searchFields: EntityField[];
  createFields: EntityField[];
  list: (
    criteria: Record<string, string>,
    page: number,
    size: number,
  ) => Observable<EntityPageData>;
  create: (value: Record<string, EntityFormValue>) => Observable<EntityRecord>;
  getById: (id: string) => Observable<EntityRecord>;
}

export type EntitySelectOptions = Record<string, EntitySelectOption[]>;
