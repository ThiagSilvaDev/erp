import { defineConfig, type Options } from 'orval';

const inputTarget = 'http://localhost:8080/v3/api-docs';

const moduleConfig = (tag: string, folder: string, title: string): Options => ({
  input: {
    target: inputTarget,
    filters: { tags: [tag] },
  },
  output: {
    mode: 'split',
    target: `src/app/api/modules/${folder}/${folder}.ts`,
    schemas: `src/app/api/modules/${folder}/dtos`,
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
  authentication: moduleConfig('authentication', 'authentication', 'Authentication'),
  catalog: moduleConfig('catalog', 'catalog', 'Catalog'),
  companies: moduleConfig('companies', 'companies', 'Companies'),
  customers: moduleConfig('customers', 'customers', 'Customers'),
  suppliers: moduleConfig('suppliers', 'suppliers', 'Suppliers'),
  contactPersons: moduleConfig('contact-persons', 'contact-persons', 'ContactPersons'),
  inventory: moduleConfig('inventory', 'inventory', 'Inventory'),
  sales: moduleConfig('sales', 'sales', 'Sales'),
  financial: moduleConfig('financial', 'financial', 'Financial'),
  users: moduleConfig('users', 'users', 'Users'),
});
