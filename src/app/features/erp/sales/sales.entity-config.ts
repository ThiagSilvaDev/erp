import { inject } from '@angular/core';
import { map } from 'rxjs';

import { SalesService } from '@generated-api/modules/sales/Sales.service';
import {
  CreateSalesOrderRequestFulfillmentType,
  CreateSalesOrderRequestPaymentMethod,
  CreateSalesOrderRequestSaleType,
  DeliveryRouteRequest,
} from '@generated-api/modules/sales/dtos';
import {
  fulfillmentTypeOptions,
  orderStatusOptions,
  paymentMethodOptions,
} from '../entity/config/entity-field-options';
import {
  CompaniesOptionLoader,
  CustomersOptionLoader,
  ProductsOptionLoader,
  WarehousesOptionLoader,
} from '../entity/data-access/entity-option-loaders';
import { EntityConfig, EntityCreateValue } from '../entity/models/entity-view-model';
import { provideEntityFeature } from '../entity/providers/entity-feature.provider';
import { getValue, optionalNumber, pageData, text } from '../entity/utils/entity-utils';

function salesOrderItems(value: EntityCreateValue) {
  const items = value['items'];
  if (Array.isArray(items)) {
    return items.map((item) => ({
      productId: text(item.productId),
      warehouseId: text(item.warehouseId),
      quantity: optionalNumber(item.quantity),
    }));
  }

  return [
    {
      productId: text(value['productId']),
      warehouseId: text(value['warehouseId']),
      quantity: optionalNumber(value['quantity']),
    },
  ];
}

function deliveryRoute(value: EntityCreateValue): DeliveryRouteRequest | undefined {
  if (text(value['fulfillmentType']) !== CreateSalesOrderRequestFulfillmentType.DELIVERY) {
    return undefined;
  }

  const routeName = text(value['deliveryRouteName']);
  const originWarehouseId =
    text(value['deliveryRouteOriginWarehouseId']) || salesOrderItems(value)[0]?.warehouseId;
  const destinationDescription = text(value['deliveryRouteDestinationDescription']);

  if (!routeName || !originWarehouseId || !destinationDescription) {
    return undefined;
  }

  return {
    routeName,
    originWarehouseId,
    destinationDescription,
  };
}

export function salesOrdersEntityConfig(): EntityConfig {
  const sales = inject(SalesService);
  return {
    key: 'sales-orders',
    title: 'Sales orders',
    description: 'Customer orders, totals, and fulfillment state.',
    createMode: 'page',
    columns: [
      { key: 'orderNumber', label: 'Order' },
      { key: 'sellerSummary.companyName', label: 'Seller company Name' },
      { key: 'buyerSummary.buyerName', label: 'Buyer company ID' },
      { key: 'formattedTotalAmount', label: 'Total' },
      { key: 'status', label: 'Status' },
    ],
    detailFields: [
      { key: 'orderNumber', label: 'Order' },
      { key: 'saleType', label: 'Sale type' },
      { key: 'sellerSummary.companyName', label: 'Seller company Name' },
      { key: 'buyerSummary.buyerName', label: 'Buyer company ID' },
      { key: 'customerId', label: 'Customer ID' },
      { key: 'status', label: 'Status' },
      { key: 'formattedTotalAmount', label: 'Total' },
      { key: 'deliveryRoute.routeName', label: 'Delivery route' },
      { key: 'deliveryRoute.originWarehouseId', label: 'Route warehouse ID' },
      { key: 'deliveryRoute.destinationDescription', label: 'Delivery destination' },
      { key: 'items.length', label: 'Items' },
    ],
    searchFields: [
      { key: 'customerId', label: 'Customer', type: 'select', optionsKey: 'customers' },
      { key: 'status', label: 'Status', type: 'select', options: orderStatusOptions },
    ],
    createFields: [
      {
        key: 'customerId',
        label: 'Customer',
        type: 'select',
        optionsKey: 'customers',
        section: 'Customer',
      },
      {
        key: 'paymentMethod',
        label: 'Payment',
        required: true,
        type: 'select',
        options: paymentMethodOptions,
        section: 'Order',
      },
      {
        key: 'fulfillmentType',
        label: 'Fulfillment',
        required: true,
        type: 'radio',
        options: fulfillmentTypeOptions,
        section: 'Order',
      },
      {
        key: 'deliveryRouteName',
        label: 'Delivery route',
        section: 'Delivery',
      },
      {
        key: 'deliveryRouteDestinationDescription',
        label: 'Destination',
        type: 'textarea',
        section: 'Delivery',
      },
      {
        key: 'productId',
        label: 'Product',
        required: true,
        type: 'select',
        optionsKey: 'products',
        section: 'Item',
      },
      {
        key: 'warehouseId',
        label: 'Warehouse',
        required: true,
        type: 'select',
        optionsKey: 'warehouses',
        section: 'Item',
      },
      { key: 'quantity', label: 'Quantity', required: true, type: 'number', section: 'Item' },
    ],
    list: (criteria, page, size) =>
      sales.listSalesOrders({ ...criteria, page, size }).pipe(map(pageData)),
    create: (value) =>
      sales.createSalesOrder({
        saleType: CreateSalesOrderRequestSaleType.B2C,
        paymentMethod:
          (text(value['paymentMethod']) as CreateSalesOrderRequestPaymentMethod) ||
          CreateSalesOrderRequestPaymentMethod.CASH,
        fulfillmentType:
          (text(value['fulfillmentType']) as CreateSalesOrderRequestFulfillmentType) ||
          CreateSalesOrderRequestFulfillmentType.PICKUP,
        customerId: text(value['customerId']) || undefined,
        deliveryRoute: deliveryRoute(value),
        items: salesOrderItems(value),
      }),
    getById: (id) => sales.getSalesOrderById(id),
    actions: [
      {
        label: 'Confirm',
        isVisible: (record) => getValue(record, 'status') === 'DRAFT',
        run: (record) => sales.confirmSalesOrder(text('id' in record ? record.id : '')),
      },
      {
        label: 'Cancel',
        isVisible: (record) => getValue(record, 'status') !== 'CANCELLED',
        run: (record) => sales.cancelSalesOrder(text('id' in record ? record.id : '')),
      },
    ],
  };
}

export const provideSalesOrderListEntityConfig = () =>
  provideEntityFeature({
    config: salesOrdersEntityConfig,
    optionLoaders: [CompaniesOptionLoader, CustomersOptionLoader],
  });

export const provideSalesOrderCreateEntityConfig = () =>
  provideEntityFeature({
    config: salesOrdersEntityConfig,
    optionLoaders: [
      CompaniesOptionLoader,
      CustomersOptionLoader,
      ProductsOptionLoader,
      WarehousesOptionLoader,
    ],
  });
