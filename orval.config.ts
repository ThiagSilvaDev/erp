import { defineConfig, type Options } from 'orval';

const inputTarget = 'http://localhost:8080/v3/api-docs';

const toTags = (tags: string | string[]) => (Array.isArray(tags) ? tags : [tags]);

const moduleConfig = (
  tags: string | string[],
  folder: string,
  fileName: string,
  title: string,
): Options => ({
  input: {
    target: inputTarget,
    filters: {
      tags: toTags(tags),
    },
  },
  output: {
    mode: 'split',
    target: `src/app/generated/api/modules/${folder}/${fileName}.ts`,
    schemas: `src/app/generated/api/modules/${folder}/dtos`,
    client: 'angular',
    clean: true,
    override: {
      title: () => title,
      angular: {
        provideIn: 'root',
      },
    },
  },
  hooks: {
    afterAllFilesWrite: 'prettier --write',
  },
});

export default defineConfig({
  authentication: moduleConfig(
    'authentication',
    'authentication',
    'Authentication',
    'Authentication',
  ),
  catalog: moduleConfig('catalog', 'catalog', 'Catalog', 'Catalog'),
  companies: moduleConfig('companies', 'companies', 'Companies', 'Companies'),
  customers: moduleConfig('customers', 'customers', 'Customers', 'Customers'),
  suppliers: moduleConfig('suppliers', 'suppliers', 'Suppliers', 'Suppliers'),
  contactPersons: moduleConfig(
    'contact-persons',
    'contact-persons',
    'ContactPersons',
    'ContactPersons',
  ),
  inventory: moduleConfig('inventory', 'inventory', 'Inventory', 'Inventory'),
  kpis: moduleConfig('kpis', 'kpis', 'Kpis', 'Kpis'),
  sales: moduleConfig('sales', 'sales', 'Sales', 'Sales'),
  users: moduleConfig('users', 'users', 'Users', 'Users'),
  logistics: moduleConfig('logistics', 'logistics', 'Logistics', 'Logistics'),

  financial: moduleConfig(
    ['ledger entries', 'financial invoices', 'financial payments'],
    'financial',
    'Financial',
    'Financial',
  ),
});
