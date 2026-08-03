const LOGIN_SEGMENT = 'login';
const SELECT_COMPANY_SEGMENT = 'select-company';
const DASHBOARD_SEGMENT = 'dashboard';
const DRIVER_SEGMENT = 'driver';
const DRIVER_SHIPMENTS_SEGMENT = 'shipments';

const ACCOUNTING_ACCOUNTS_SEGMENT = 'accounting-accounts';
const ACCOUNTING_ENTRIES_SEGMENT = 'accounting-entries';
const ACCOUNTING_BALANCES_SEGMENT = 'accounting-balances';
const CATEGORIES_SEGMENT = 'categories';
const COMPANIES_SEGMENT = 'companies';
const CONTACT_PERSONS_SEGMENT = 'contact-persons';
const CUSTOMERS_SEGMENT = 'customers';
const FINANCIAL_ENTRIES_SEGMENT = 'financial-entries';
const INVENTORY_ITEMS_SEGMENT = 'inventory-items';
const INVOICES_SEGMENT = 'invoices';
const PAYMENTS_SEGMENT = 'payments';
const PRODUCTS_SEGMENT = 'products';
const RESERVATIONS_SEGMENT = 'reservations';
const SALES_ORDERS_SEGMENT = 'sales-orders';
const STOCK_MOVEMENTS_SEGMENT = 'stock-movements';
const SUPPLIERS_SEGMENT = 'suppliers';
const USERS_SEGMENT = 'users';
const WAREHOUSES_SEGMENT = 'warehouses';

export const appRoutePaths = {
  login: LOGIN_SEGMENT,
  selectCompany: SELECT_COMPANY_SEGMENT,
  dashboard: DASHBOARD_SEGMENT,
  driver: DRIVER_SEGMENT,
  driverShipments: `${DRIVER_SEGMENT}/${DRIVER_SHIPMENTS_SEGMENT}`,
  accountingAccounts: ACCOUNTING_ACCOUNTS_SEGMENT,
  accountingEntries: ACCOUNTING_ENTRIES_SEGMENT,
  accountingBalances: ACCOUNTING_BALANCES_SEGMENT,
  categories: CATEGORIES_SEGMENT,
  companies: COMPANIES_SEGMENT,
  contactPersons: CONTACT_PERSONS_SEGMENT,
  customers: CUSTOMERS_SEGMENT,
  financialEntries: FINANCIAL_ENTRIES_SEGMENT,
  inventoryItems: INVENTORY_ITEMS_SEGMENT,
  invoices: INVOICES_SEGMENT,
  payments: PAYMENTS_SEGMENT,
  products: PRODUCTS_SEGMENT,
  reservations: RESERVATIONS_SEGMENT,
  salesOrders: SALES_ORDERS_SEGMENT,
  stockMovements: STOCK_MOVEMENTS_SEGMENT,
  suppliers: SUPPLIERS_SEGMENT,
  users: USERS_SEGMENT,
  warehouses: WAREHOUSES_SEGMENT,
} as const;

export const appPaths = {
  root: '/',
  login: `/${LOGIN_SEGMENT}`,
  selectCompany: `/${SELECT_COMPANY_SEGMENT}`,
  dashboard: `/${DASHBOARD_SEGMENT}`,
  driver: `/${DRIVER_SEGMENT}`,
  driverShipments: `/${DRIVER_SEGMENT}/${DRIVER_SHIPMENTS_SEGMENT}`,
  accountingAccounts: `/${ACCOUNTING_ACCOUNTS_SEGMENT}`,
  accountingEntries: `/${ACCOUNTING_ENTRIES_SEGMENT}`,
  accountingBalances: `/${ACCOUNTING_BALANCES_SEGMENT}`,
  categories: `/${CATEGORIES_SEGMENT}`,
  companies: `/${COMPANIES_SEGMENT}`,
  contactPersons: `/${CONTACT_PERSONS_SEGMENT}`,
  customers: `/${CUSTOMERS_SEGMENT}`,
  financialEntries: `/${FINANCIAL_ENTRIES_SEGMENT}`,
  inventoryItems: `/${INVENTORY_ITEMS_SEGMENT}`,
  invoices: `/${INVOICES_SEGMENT}`,
  payments: `/${PAYMENTS_SEGMENT}`,
  products: `/${PRODUCTS_SEGMENT}`,
  reservations: `/${RESERVATIONS_SEGMENT}`,
  salesOrders: `/${SALES_ORDERS_SEGMENT}`,
  stockMovements: `/${STOCK_MOVEMENTS_SEGMENT}`,
  suppliers: `/${SUPPLIERS_SEGMENT}`,
  users: `/${USERS_SEGMENT}`,
  warehouses: `/${WAREHOUSES_SEGMENT}`,

  // Detail paths (if needed by the application in the future)
  categoryDetail: (id: string | number) => `/${CATEGORIES_SEGMENT}/${id}`,
  companyDetail: (id: string | number) => `/${COMPANIES_SEGMENT}/${id}`,
  customerDetail: (id: string | number) => `/${CUSTOMERS_SEGMENT}/${id}`,
  productDetail: (id: string | number) => `/${PRODUCTS_SEGMENT}/${id}`,
  salesOrderDetail: (id: string | number) => `/${SALES_ORDERS_SEGMENT}/${id}`,
  userDetail: (id: string | number) => `/${USERS_SEGMENT}/${id}`,
  invoiceDetail: (id: string | number) => `/${INVOICES_SEGMENT}/${id}`,
} as const;
