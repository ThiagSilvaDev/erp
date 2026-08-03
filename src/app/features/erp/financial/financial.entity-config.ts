import { inject } from '@angular/core';
import { defer, map, of, tap, throwError } from 'rxjs';

import { FinancialService } from '@generated-api/modules/financial/Financial.service';
import {
  AccountingEntryResponse,
  CreateAccountingAccountRequestSystemRole,
  CreateAccountingAccountRequestType,
  CreateAccountingEntryRequestDirection,
  CreateInvoiceRequestBillingPartyType,
  CreatePaymentRequestMethod,
  CreatePaymentRequestStatus,
} from '@generated-api/modules/financial/dtos';
import {
  accountingAccountTypeOptions,
  accountingEntryDirectionOptions,
  accountingSystemRoleOptions,
  booleanOptions,
  paymentMethodOptions,
  paymentStatusOptions,
} from '../entity/config/entity-field-options';
import {
  AccountingAccountsOptionLoader,
  CustomersOptionLoader,
  InvoicesOptionLoader,
  SalesOrdersOptionLoader,
} from '../entity/data-access/entity-option-loaders';
import { EntityConfig } from '../entity/models/entity-view-model';
import { provideEntityFeature } from '../entity/providers/entity-feature.provider';
import { pageData, text } from '../entity/utils/entity-utils';

export function invoicesEntityConfig(): EntityConfig {
  const financial = inject(FinancialService);
  return {
    key: 'invoices',
    title: 'Invoices',
    description: 'Customer invoices and payment state.',
    columns: [
      { key: 'billingPartyId', label: 'Billing party ID' },
      { key: 'amount', label: 'Amount' },
      { key: 'currencyCode', label: 'Currency' },
      { key: 'dueDate', label: 'Due date' },
      { key: 'status', label: 'Status' },
    ],
    detailFields: [
      { key: 'id', label: 'Invoice ID' },
      { key: 'billingPartyType', label: 'Billing party type' },
      { key: 'billingPartyId', label: 'Billing party ID' },
      { key: 'salesOrderId', label: 'Sales order ID' },
      { key: 'amount', label: 'Amount' },
      { key: 'currencyCode', label: 'Currency' },
      { key: 'dueDate', label: 'Due date' },
      { key: 'status', label: 'Status' },
    ],
    searchFields: [],
    createFields: [
      {
        key: 'billingPartyId',
        label: 'Billing party',
        required: true,
        type: 'select',
        optionsKey: 'customers',
      },
      { key: 'salesOrderId', label: 'Sales order', type: 'select', optionsKey: 'salesOrders' },
      { key: 'amount', label: 'Amount', required: true, type: 'number' },
      { key: 'currencyCode', label: 'Currency', required: true },
      { key: 'dueDate', label: 'Due date', required: true, type: 'date' },
    ],
    list: (_criteria, page, size) => financial.listInvoices({ page, size }).pipe(map(pageData)),
    create: (value) =>
      financial.createInvoice({
        billingPartyType: CreateInvoiceRequestBillingPartyType.CUSTOMER,
        billingPartyId: text(value['billingPartyId']),
        salesOrderId: text(value['salesOrderId']) || undefined,
        amount: Number(value['amount']),
        currencyCode: text(value['currencyCode']),
        dueDate: text(value['dueDate']),
      }),
    getById: (id) => financial.getInvoiceById(id),
  };
}

export function paymentsEntityConfig(): EntityConfig {
  const financial = inject(FinancialService);
  return {
    key: 'payments',
    title: 'Payments',
    description: 'Invoice payments received by date.',
    columns: [
      { key: 'invoiceId', label: 'Invoice ID' },
      { key: 'amount', label: 'Amount' },
      { key: 'paidAt', label: 'Paid at' },
    ],
    detailFields: [
      { key: 'id', label: 'Payment ID' },
      { key: 'salesOrderId', label: 'Sales order ID' },
      { key: 'customerId', label: 'Customer ID' },
      { key: 'invoiceId', label: 'Invoice ID' },
      { key: 'amount', label: 'Amount' },
      { key: 'method', label: 'Method' },
      { key: 'status', label: 'Status' },
      { key: 'paidAt', label: 'Paid at' },
    ],
    searchFields: [],
    createFields: [
      {
        key: 'salesOrderId',
        label: 'Sales order',
        required: true,
        type: 'select',
        optionsKey: 'salesOrders',
      },
      {
        key: 'customerId',
        label: 'Customer',
        required: true,
        type: 'select',
        optionsKey: 'customers',
      },
      {
        key: 'invoiceId',
        label: 'Invoice',
        type: 'select',
        optionsKey: 'invoices',
      },
      { key: 'amount', label: 'Amount', required: true, type: 'number' },
      {
        key: 'method',
        label: 'Method',
        required: true,
        type: 'select',
        options: paymentMethodOptions,
      },
      {
        key: 'status',
        label: 'Status',
        required: true,
        type: 'select',
        options: paymentStatusOptions,
      },
      { key: 'paidAt', label: 'Paid at', type: 'date' },
    ],
    list: (_criteria, page, size) => financial.listPayments({ page, size }).pipe(map(pageData)),
    create: (value) =>
      financial.createPayment({
        salesOrderId: text(value['salesOrderId']),
        customerId: text(value['customerId']),
        invoiceId: text(value['invoiceId']) || undefined,
        amount: Number(value['amount']),
        method:
          (text(value['method']) as CreatePaymentRequestMethod) || CreatePaymentRequestMethod.CASH,
        status:
          (text(value['status']) as CreatePaymentRequestStatus) || CreatePaymentRequestStatus.PAID,
        paidAt: text(value['paidAt']) || undefined,
      }),
    getById: (id) => financial.getPaymentById(id),
  };
}

export function financialEntriesEntityConfig(): EntityConfig {
  const financial = inject(FinancialService);
  return {
    key: 'financial-entries',
    title: 'Financial entries',
    description: 'Receivable and payable ledger entries.',
    readonly: true,
    columns: [
      { key: 'type', label: 'Type' },
      { key: 'invoiceId', label: 'Invoice ID' },
      { key: 'amount', label: 'Amount' },
      { key: 'currencyCode', label: 'Currency' },
      { key: 'status', label: 'Status' },
    ],
    detailFields: [
      { key: 'id', label: 'Entry ID' },
      { key: 'type', label: 'Type' },
      { key: 'invoiceId', label: 'Invoice ID' },
      { key: 'amount', label: 'Amount' },
      { key: 'currencyCode', label: 'Currency' },
      { key: 'dueDate', label: 'Due date' },
      { key: 'status', label: 'Status' },
    ],
    searchFields: [],
    createFields: [],
    list: (_criteria, page, size) =>
      financial.listLedgerEntries({ page, size }).pipe(map(pageData)),
    getById: (id) => financial.getLedgerEntryById(id),
  };
}

export function accountingAccountsEntityConfig(): EntityConfig {
  const financial = inject(FinancialService);
  return {
    key: 'accounting-accounts',
    title: 'Chart of accounts',
    description: 'Accounts used for competence accounting postings.',
    columns: [
      { key: 'code', label: 'Code' },
      { key: 'name', label: 'Name' },
      { key: 'type', label: 'Type' },
      { key: 'systemRole', label: 'System role' },
      { key: 'status', label: 'Status' },
    ],
    detailFields: [
      { key: 'id', label: 'Account ID' },
      { key: 'code', label: 'Code' },
      { key: 'name', label: 'Name' },
      { key: 'description', label: 'Description' },
      { key: 'parentId', label: 'Parent account ID' },
      { key: 'type', label: 'Type' },
      { key: 'systemRole', label: 'System role' },
      { key: 'acceptsPostings', label: 'Accepts postings' },
      { key: 'status', label: 'Status' },
    ],
    searchFields: [],
    createFields: [
      { key: 'code', label: 'Code', required: true },
      { key: 'name', label: 'Name', required: true },
      { key: 'description', label: 'Description', type: 'textarea' },
      {
        key: 'parentId',
        label: 'Parent account',
        type: 'select',
        optionsKey: 'accountingAccounts',
      },
      {
        key: 'type',
        label: 'Type',
        required: true,
        type: 'select',
        options: accountingAccountTypeOptions,
      },
      {
        key: 'systemRole',
        label: 'System role',
        type: 'select',
        options: accountingSystemRoleOptions,
      },
      {
        key: 'acceptsPostings',
        label: 'Accepts postings',
        type: 'select',
        options: booleanOptions,
      },
    ],
    list: (_criteria, page, size) =>
      financial.listAccountingAccounts({ page, size }).pipe(map(pageData)),
    create: (value) =>
      financial.createAccountingAccount({
        code: text(value['code']),
        name: text(value['name']),
        description: text(value['description']) || undefined,
        parentId: text(value['parentId']) || undefined,
        type: text(value['type']) as CreateAccountingAccountRequestType,
        systemRole:
          (text(value['systemRole']) as CreateAccountingAccountRequestSystemRole) || undefined,
        acceptsPostings: optionalBoolean(value['acceptsPostings']),
      }),
    getById: (id) => financial.getAccountingAccountById(id),
  };
}

export function accountingEntriesEntityConfig(): EntityConfig {
  const financial = inject(FinancialService);
  const recordsById = new Map<string, AccountingEntryResponse>();

  return {
    key: 'accounting-entries',
    title: 'Accounting entries',
    description: 'Competence accounting journal entries from manual and automatic postings.',
    columns: [
      { key: 'competenceDate', label: 'Competence date' },
      { key: 'accountId', label: 'Account ID' },
      { key: 'direction', label: 'Direction' },
      { key: 'amount', label: 'Amount' },
      { key: 'currencyCode', label: 'Currency' },
      { key: 'sourceType', label: 'Source' },
      { key: 'status', label: 'Status' },
    ],
    detailFields: [
      { key: 'id', label: 'Entry ID' },
      { key: 'accountId', label: 'Account ID' },
      { key: 'competenceDate', label: 'Competence date' },
      { key: 'amount', label: 'Amount' },
      { key: 'currencyCode', label: 'Currency' },
      { key: 'direction', label: 'Direction' },
      { key: 'sourceType', label: 'Source type' },
      { key: 'sourceId', label: 'Source ID' },
      { key: 'description', label: 'Description' },
      { key: 'status', label: 'Status' },
    ],
    searchFields: [
      {
        key: 'accountId',
        label: 'Account',
        type: 'select',
        optionsKey: 'accountingAccounts',
      },
      { key: 'from', label: 'From', type: 'date' },
      { key: 'to', label: 'To', type: 'date' },
    ],
    createFields: [
      {
        key: 'accountId',
        label: 'Account',
        required: true,
        type: 'select',
        optionsKey: 'accountingAccounts',
      },
      { key: 'competenceDate', label: 'Competence date', required: true, type: 'date' },
      { key: 'amount', label: 'Amount', required: true, type: 'number' },
      { key: 'currencyCode', label: 'Currency', required: true, defaultValue: 'BRL' },
      {
        key: 'direction',
        label: 'Direction',
        required: true,
        type: 'select',
        options: accountingEntryDirectionOptions,
      },
      { key: 'description', label: 'Description', type: 'textarea' },
    ],
    list: (criteria, page, size) =>
      financial
        .listAccountingEntries({
          accountId: criteria['accountId'] || undefined,
          from: criteria['from'] || undefined,
          to: criteria['to'] || undefined,
          page,
          size,
        })
        .pipe(
          tap((response) => {
            for (const record of response.content ?? []) {
              if (record.id) {
                recordsById.set(record.id, record);
              }
            }
          }),
          map(pageData),
        ),
    create: (value) =>
      financial.createAccountingEntry({
        accountId: text(value['accountId']),
        competenceDate: text(value['competenceDate']),
        amount: Number(value['amount']),
        currencyCode: text(value['currencyCode']),
        direction: text(value['direction']) as CreateAccountingEntryRequestDirection,
        description: text(value['description']) || undefined,
      }),
    getById: (id) =>
      defer(() => {
        const record = recordsById.get(id);
        return record
          ? of(record)
          : throwError(() => new Error('Accounting entry details are available from the list.'));
      }),
  };
}

function optionalBoolean(value: unknown): boolean | undefined {
  const normalized = text(value);
  return normalized ? normalized === 'true' : undefined;
}

export const provideInvoicesEntityConfig = () =>
  provideEntityFeature({
    config: invoicesEntityConfig,
    optionLoaders: [CustomersOptionLoader, SalesOrdersOptionLoader],
  });

export const providePaymentsEntityConfig = () =>
  provideEntityFeature({
    config: paymentsEntityConfig,
    optionLoaders: [CustomersOptionLoader, InvoicesOptionLoader, SalesOrdersOptionLoader],
  });

export const provideFinancialEntriesEntityConfig = () =>
  provideEntityFeature({ config: financialEntriesEntityConfig, optionLoaders: [] });

export const provideAccountingAccountsEntityConfig = () =>
  provideEntityFeature({
    config: accountingAccountsEntityConfig,
    optionLoaders: [AccountingAccountsOptionLoader],
  });

export const provideAccountingEntriesEntityConfig = () =>
  provideEntityFeature({
    config: accountingEntriesEntityConfig,
    optionLoaders: [AccountingAccountsOptionLoader],
  });
