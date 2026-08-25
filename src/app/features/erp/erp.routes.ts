import { Provider } from '@angular/core';
import { Routes } from '@angular/router';

import {
  provideCategoriesEntityConfig,
  provideProductsEntityConfig,
} from './catalog/catalog.entity-config';
import { provideCompaniesEntityConfig } from './companies/companies.entity-config';
import { provideCompanyContactsEntityConfig } from './company-contacts/company-contacts.entity-config';
import { provideContactPersonsEntityConfig } from './contact-persons/contact-persons.entity-config';
import { provideCustomersEntityConfig } from './customers/customers.entity-config';
import {
  provideAccountingAccountsEntityConfig,
  provideAccountingEntriesEntityConfig,
  provideFinancialEntriesEntityConfig,
  provideInvoicesEntityConfig,
  providePaymentsEntityConfig,
} from './financial/financial.entity-config';
import {
  provideInventoryItemsEntityConfig,
  provideReservationsEntityConfig,
  provideStockMovementsEntityConfig,
  provideWarehousesEntityConfig,
} from './inventory/inventory.entity-config';
import {
  provideSalesOrderCreateEntityConfig,
  provideSalesOrderListEntityConfig,
} from './sales/sales.entity-config';
import { provideSuppliersEntityConfig } from './suppliers/suppliers.entity-config';
import { provideUsersEntityConfig } from './users/users.entity-config';
import { EntityKey } from './entity/models/entity-view-model';

const entityPage = () => import('./entity/pages/entity-page/entity-page').then((m) => m.EntityPage);
const entityCreatePage = () =>
  import('./entity/pages/entity-create-page/entity-create-page').then((m) => m.EntityCreatePage);
const logisticsPage = () =>
  import('./logistics/pages/logistics-page/logistics-page').then((m) => m.LogisticsPage);
const shipmentDetailPage = () =>
  import('./logistics/pages/shipment-detail-page/shipment-detail-page').then(
    (m) => m.ShipmentDetailPage,
  );
const salesOrderDetailPage = () =>
  import('./sales/pages/sales-order-detail-page/sales-order-detail-page').then(
    (m) => m.SalesOrderDetailPage,
  );

function entityRoute(
  path: EntityKey,
  listProviders: Provider[],
  createProviders?: Provider[],
): Routes[number] {
  return {
    path,
    children: [
      ...(createProviders
        ? [
            {
              path: 'new',
              data: { entity: path },
              providers: createProviders,
              loadComponent: entityCreatePage,
            },
          ]
        : []),
      {
        path: '',
        data: { entity: path },
        providers: listProviders,
        loadComponent: entityPage,
      },
    ],
  };
}

export const ERP_ROUTES: Routes = [
  entityRoute('companies', provideCompaniesEntityConfig(), provideCompaniesEntityConfig()),
  {
    path: 'users',
    children: [
      {
        path: 'new',
        loadComponent: () =>
          import('./users/pages/user-create-page/user-create-page').then((m) => m.UserCreatePage),
      },
      {
        path: '',
        data: { entity: 'users' as EntityKey },
        providers: provideUsersEntityConfig(),
        loadComponent: entityPage,
      },
    ],
  },
  {
    path: 'iam/roles',
    children: [
      {
        path: 'new',
        loadComponent: () =>
          import('@features/iam/pages/role-editor-page/role-editor-page').then(
            (m) => m.RoleEditorPage,
          ),
      },
      {
        path: ':roleId',
        loadComponent: () =>
          import('@features/iam/pages/role-editor-page/role-editor-page').then(
            (m) => m.RoleEditorPage,
          ),
      },
      {
        path: '',
        loadComponent: () =>
          import('@features/iam/pages/iam-roles-page/iam-roles-page').then((m) => m.IamRolesPage),
      },
    ],
  },
  {
    path: 'iam/memberships',
    loadComponent: () =>
      import('@features/iam/pages/iam-memberships-page/iam-memberships-page').then(
        (m) => m.IamMembershipsPage,
      ),
  },
  entityRoute('categories', provideCategoriesEntityConfig()),
  entityRoute('products', provideProductsEntityConfig(), provideProductsEntityConfig()),
  entityRoute('customers', provideCustomersEntityConfig(), provideCustomersEntityConfig()),
  entityRoute('suppliers', provideSuppliersEntityConfig(), provideSuppliersEntityConfig()),
  entityRoute('company-contacts', provideCompanyContactsEntityConfig()),
  entityRoute('contact-persons', provideContactPersonsEntityConfig()),
  {
    path: 'pdv',
    loadComponent: () => import('./pdv/pages/pdv-page/pdv-page').then((m) => m.PdvPage),
  },
  {
    path: 'sales-orders',
    children: [
      {
        path: 'new',
        data: { entity: 'sales-orders' as EntityKey },
        providers: provideSalesOrderCreateEntityConfig(),
        loadComponent: entityCreatePage,
      },
      {
        path: ':id',
        loadComponent: salesOrderDetailPage,
      },
      {
        path: '',
        data: { entity: 'sales-orders' as EntityKey },
        providers: provideSalesOrderListEntityConfig(),
        loadComponent: entityPage,
      },
    ],
  },
  {
    path: 'logistics',
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'vehicles' },
      { path: 'vehicles', data: { logisticsTab: 'vehicles' }, loadComponent: logisticsPage },
      { path: 'drivers', data: { logisticsTab: 'drivers' }, loadComponent: logisticsPage },
      { path: 'shipments/:id', loadComponent: shipmentDetailPage },
      { path: 'shipments', data: { logisticsTab: 'shipments' }, loadComponent: logisticsPage },
    ],
  },
  entityRoute('warehouses', provideWarehousesEntityConfig()),
  entityRoute('inventory-items', provideInventoryItemsEntityConfig()),
  entityRoute('stock-movements', provideStockMovementsEntityConfig()),
  entityRoute('reservations', provideReservationsEntityConfig()),
  entityRoute('invoices', provideInvoicesEntityConfig()),
  entityRoute('payments', providePaymentsEntityConfig()),
  entityRoute('financial-entries', provideFinancialEntriesEntityConfig()),
  entityRoute('accounting-accounts', provideAccountingAccountsEntityConfig()),
  entityRoute('accounting-entries', provideAccountingEntriesEntityConfig()),
  {
    path: 'accounting-balances',
    loadComponent: () =>
      import('./financial/pages/account-balances-page/account-balances-page').then(
        (m) => m.AccountBalancesPage,
      ),
  },
];
