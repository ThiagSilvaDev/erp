export const activeStatusOptions = [
  { label: 'Active', value: 'ACTIVE' },
  { label: 'Inactive', value: 'INACTIVE' },
];

export const companyTypeOptions = [
  { label: 'Factory', value: 'FACTORY' },
  { label: 'Correspondent', value: 'CORRESPONDENT' },
];

export const orderStatusOptions = [
  { label: 'Draft', value: 'DRAFT' },
  { label: 'Confirmed', value: 'CONFIRMED' },
  { label: 'Cancelled', value: 'CANCELLED' },
];

export const paymentMethodOptions = [
  { label: 'Cash', value: 'CASH' },
  { label: 'PIX', value: 'PIX' },
  { label: 'Credit card', value: 'CREDIT_CARD' },
  { label: 'Debit card', value: 'DEBIT_CARD' },
  { label: 'Bank transfer', value: 'BANK_TRANSFER' },
  { label: 'Boleto', value: 'BOLETO' },
];

export const fulfillmentTypeOptions = [
  { label: 'Pickup', value: 'PICKUP' },
  { label: 'Delivery', value: 'DELIVERY' },
];

export const paymentStatusOptions = [
  { label: 'Pending', value: 'PENDING' },
  { label: 'Authorized', value: 'AUTHORIZED' },
  { label: 'Paid', value: 'PAID' },
  { label: 'Failed', value: 'FAILED' },
  { label: 'Cancelled', value: 'CANCELLED' },
  { label: 'Refunded', value: 'REFUNDED' },
];

export const accountingAccountTypeOptions = [
  { label: 'Asset', value: CreateAccountingAccountRequestType.ASSET },
  { label: 'Liability', value: CreateAccountingAccountRequestType.LIABILITY },
  { label: 'Equity', value: CreateAccountingAccountRequestType.EQUITY },
  { label: 'Revenue', value: CreateAccountingAccountRequestType.REVENUE },
  { label: 'Expense', value: CreateAccountingAccountRequestType.EXPENSE },
];

export const accountingSystemRoleOptions = [
  { label: 'Sales revenue', value: CreateAccountingAccountRequestSystemRole.REVENUE_SALES },
  { label: 'Cost of goods sold', value: CreateAccountingAccountRequestSystemRole.COGS },
  {
    label: 'Administrative expense',
    value: CreateAccountingAccountRequestSystemRole.ADMIN_EXPENSE,
  },
  { label: 'Sales expense', value: CreateAccountingAccountRequestSystemRole.SALES_EXPENSE },
  { label: 'Financial revenue', value: CreateAccountingAccountRequestSystemRole.FINANCIAL_REVENUE },
  { label: 'Financial expense', value: CreateAccountingAccountRequestSystemRole.FINANCIAL_EXPENSE },
];

export const accountingEntryDirectionOptions = [
  { label: 'Debit', value: CreateAccountingEntryRequestDirection.DEBIT },
  { label: 'Credit', value: CreateAccountingEntryRequestDirection.CREDIT },
];

export const booleanOptions = [
  { label: 'Yes', value: 'true' },
  { label: 'No', value: 'false' },
];
import {
  CreateAccountingAccountRequestSystemRole,
  CreateAccountingAccountRequestType,
  CreateAccountingEntryRequestDirection,
} from '@generated-api/modules/financial/dtos';
