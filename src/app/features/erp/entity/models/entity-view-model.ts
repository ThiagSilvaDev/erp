import { FormControl, FormGroup } from '@angular/forms';
import { Observable } from 'rxjs';
import { Type } from '@angular/core';

export type EntityKey =
  | 'categories'
  | 'companies'
  | 'company-contacts'
  | 'contact-persons'
  | 'customers'
  | 'financial-entries'
  | 'inventory-items'
  | 'invoices'
  | 'payments'
  | 'products'
  | 'reservations'
  | 'sales-orders'
  | 'stock-movements'
  | 'suppliers'
  | 'users'
  | 'warehouses';

export type EntityRecord = object;

export type EntityFormValue = string | number;
export interface EntitySalesOrderItemFormValue {
  productId: string;
  warehouseId: string;
  quantity: number;
}
export type EntityCreateValue = Record<
  string,
  EntityFormValue | EntitySalesOrderItemFormValue[]
>;
export type EntityForm = FormGroup<Record<string, FormControl<EntityFormValue>>>;

export interface EntityPageData {
  content: EntityRecord[];
  totalElements: number;
}

export interface EntitySelectOption {
  label: string;
  value: string;
}

export interface EntityFieldOption {
  label: string;
  value: string;
}

export interface EntityField {
  key: string;
  label: string;
  required?: boolean;
  section?: string;
  type?: 'date' | 'email' | 'number' | 'password' | 'radio' | 'select' | 'textarea';
  options?: EntityFieldOption[];
  optionsKey?:
    | 'categories'
    | 'companies'
    | 'customers'
    | 'invoices'
    | 'products'
    | 'salesOrders'
    | 'warehouses';
}

export type EntityOptionsKey = NonNullable<EntityField['optionsKey']>;

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
  createMode?: 'modal' | 'page';
  readonly?: boolean;
  list: (
    criteria: Record<string, string>,
    page: number,
    size: number,
  ) => Observable<EntityPageData>;
  create?: (value: EntityCreateValue) => Observable<EntityRecord>;
  getById: (id: string) => Observable<EntityRecord>;
  actions?: EntityAction[];
}

export interface EntityAction {
  label: string;
  icon?: string;
  run: (record: EntityRecord) => Observable<EntityRecord>;
  isVisible?: (record: EntityRecord) => boolean;
  refreshAfterRun?: boolean;
  successMessage?: string | null;
}

export type EntitySelectOptions = Partial<
  Record<EntityOptionsKey, EntitySelectOption[]>
>;

export interface EntityOptionLoader {
  key: EntityOptionsKey;
  load: () => Observable<EntitySelectOption[]>;
}

export interface EntityFeatureProviderConfig {
  config: EntityConfig | (() => EntityConfig);
  optionLoaders?: Type<EntityOptionLoader>[];
}
