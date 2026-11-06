import { inject } from '@angular/core';
import { map } from 'rxjs';

import { CatalogService } from '@generated-api/modules/catalog/Catalog.service';
import { activeStatusOptions } from '../entity/config/entity-field-options';
import { CategoriesOptionLoader } from '../entity/data-access/entity-option-loaders';
import { EntityConfig } from '../entity/models/entity-view-model';
import { provideEntityFeature } from '../entity/providers/entity-feature.provider';
import { pageData, text } from '../entity/utils/entity-utils';

export function categoriesEntityConfig(): EntityConfig {
  const catalog = inject(CatalogService);
  return {
    key: 'categories',
    title: 'Categories',
    description: 'Product category records.',
    columns: [
      { key: 'code', label: 'Code' },
      { key: 'name', label: 'Name' },
      { key: 'status', label: 'Status' },
    ],
    detailFields: [
      { key: 'code', label: 'Code' },
      { key: 'name', label: 'Name' },
      { key: 'description', label: 'Description' },
      { key: 'status', label: 'Status' },
    ],
    searchFields: [
      { key: 'code', label: 'Code' },
      { key: 'name', label: 'Name' },
      { key: 'status', label: 'Status', type: 'select', options: activeStatusOptions },
    ],
    createFields: [
      { key: 'code', label: 'Code', required: true },
      { key: 'name', label: 'Name', required: true },
      { key: 'description', label: 'Description', type: 'textarea' },
    ],
    list: (criteria, page, size) =>
      catalog.listCategories({ ...criteria, page, size }).pipe(map(pageData)),
    create: (value) =>
      catalog.createCategory({
        code: text(value['code']),
        name: text(value['name']),
        description: text(value['description']) || undefined,
      }),
    getById: (id) => catalog.getCategoryById(id),
  };
}

export function productsEntityConfig(): EntityConfig {
  const catalog = inject(CatalogService);
  return {
    key: 'products',
    title: 'Products',
    description: 'Sellable product records.',
    createMode: 'page',
    columns: [
      { key: 'sku', label: 'SKU' },
      { key: 'name', label: 'Name' },
      { key: 'category.name', label: 'Category' },
      { key: 'formattedBasePrice', label: 'Price' },
      { key: 'status', label: 'Status' },
    ],
    detailFields: [
      { key: 'sku', label: 'SKU' },
      { key: 'name', label: 'Name' },
      { key: 'description', label: 'Description' },
      { key: 'basePrice.amount', label: 'Price' },
      { key: 'basePrice.currencyCode', label: 'Currency' },
      { key: 'category.name', label: 'Category' },
      { key: 'status', label: 'Status' },
    ],
    searchFields: [
      { key: 'sku', label: 'SKU' },
      { key: 'name', label: 'Name' },
      { key: 'categoryId', label: 'Category', type: 'select', optionsKey: 'categories' },
      { key: 'status', label: 'Status', type: 'select', options: activeStatusOptions },
    ],
    createFields: [
      {
        key: 'sku',
        label: 'SKU (optional)',
        section: 'Product',
        maxLength: 80,
        hint: 'Leave blank to generate a SKU from the category when you save (for example, OFFICE-00000001). Enter your own SKU to use an existing code. Maximum 80 characters.',
      },
      { key: 'name', label: 'Name', required: true, section: 'Product' },
      { key: 'description', label: 'Description', type: 'textarea', section: 'Product' },
      { key: 'amount', label: 'Base price', required: true, type: 'number', section: 'Pricing' },
      { key: 'currencyCode', label: 'Currency', required: true, section: 'Pricing' },
      {
        key: 'categoryId',
        label: 'Category',
        required: true,
        type: 'select',
        optionsKey: 'categories',
        section: 'Pricing',
      },
    ],
    list: (criteria, page, size) =>
      catalog.listProducts({ ...criteria, page, size }).pipe(map(pageData)),
    create: (value) =>
      catalog.createProduct({
        sku: text(value['sku']).trim() || undefined,
        name: text(value['name']),
        description: text(value['description']) || undefined,
        basePrice: {
          amount: Number(value['amount']),
          currencyCode: text(value['currencyCode']),
        },
        categoryId: text(value['categoryId']),
      }),
    getById: (id) => catalog.getProductById(id),
  };
}

export const provideCategoriesEntityConfig = () =>
  provideEntityFeature({ config: categoriesEntityConfig, optionLoaders: [] });

export const provideProductsEntityConfig = () =>
  provideEntityFeature({ config: productsEntityConfig, optionLoaders: [CategoriesOptionLoader] });
