import { inject } from '@angular/core';
import { map } from 'rxjs';

import { FinancialService } from '@generated-api/modules/financial/Financial.service';
import {
  CreateInvoiceRequestBillingPartyType,
  CreatePaymentRequestMethod,
  CreatePaymentRequestStatus,
} from '@generated-api/modules/financial/dtos';
import { paymentMethodOptions, paymentStatusOptions } from '../entity/config/entity-field-options';
import {
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
