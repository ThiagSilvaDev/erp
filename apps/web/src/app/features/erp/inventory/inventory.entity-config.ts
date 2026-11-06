import { inject } from '@angular/core';
import { map } from 'rxjs';

import { InventoryService } from '@generated-api/modules/inventory/Inventory.service';
import {
  CreateStockMovementRequestReason,
  CreateStockMovementRequestType,
  type ListInventoryItemsParams,
} from '@generated-api/modules/inventory/dtos';
import {
  ProductsOptionLoader,
  SalesOrdersOptionLoader,
  WarehousesOptionLoader,
} from '../entity/data-access/entity-option-loaders';
import { EntityConfig } from '../entity/models/entity-view-model';
import { provideEntityFeature } from '../entity/providers/entity-feature.provider';
import { optionalNumber, pageData, text } from '../entity/utils/entity-utils';

const stockMovementTypeOptions = [
  { label: 'In', value: CreateStockMovementRequestType.IN },
  { label: 'Out', value: CreateStockMovementRequestType.OUT },
  { label: 'Reserve', value: CreateStockMovementRequestType.RESERVE },
  {
    label: 'Release reservation',
    value: CreateStockMovementRequestType.RELEASE_RESERVATION,
  },
  { label: 'Transfer in', value: CreateStockMovementRequestType.TRANSFER_IN },
  { label: 'Transfer out', value: CreateStockMovementRequestType.TRANSFER_OUT },
  { label: 'Adjustment', value: CreateStockMovementRequestType.ADJUSTMENT },
];

const stockMovementReasonOptions = [
  { label: 'Initial stock', value: CreateStockMovementRequestReason.INITIAL_STOCK },
  { label: 'Purchase receipt', value: CreateStockMovementRequestReason.PURCHASE_RECEIPT },
  {
    label: 'Sale order confirmed',
    value: CreateStockMovementRequestReason.SALE_ORDER_CONFIRMED,
  },
  {
    label: 'Sale order cancelled',
    value: CreateStockMovementRequestReason.SALE_ORDER_CANCELLED,
  },
  { label: 'Sale fulfilled', value: CreateStockMovementRequestReason.SALE_FULFILLED },
  { label: 'Customer return', value: CreateStockMovementRequestReason.CUSTOMER_RETURN },
  { label: 'Supplier return', value: CreateStockMovementRequestReason.SUPPLIER_RETURN },
  { label: 'Manual adjustment', value: CreateStockMovementRequestReason.MANUAL_ADJUSTMENT },
  { label: 'Inventory count', value: CreateStockMovementRequestReason.INVENTORY_COUNT },
  { label: 'Damaged goods', value: CreateStockMovementRequestReason.DAMAGED_GOODS },
  { label: 'Lost goods', value: CreateStockMovementRequestReason.LOST_GOODS },
  { label: 'Warehouse transfer', value: CreateStockMovementRequestReason.WAREHOUSE_TRANSFER },
];

export function warehousesEntityConfig(): EntityConfig {
  const inventory = inject(InventoryService);
  return {
    key: 'warehouses',
    title: 'Warehouses',
    description: 'Inventory storage locations.',
    columns: [
      { key: 'code', label: 'Code' },
      { key: 'name', label: 'Name' },
    ],
    detailFields: [
      { key: 'id', label: 'Warehouse ID' },
      { key: 'code', label: 'Code' },
      { key: 'name', label: 'Name' },
    ],
    searchFields: [],
    createFields: [
      { key: 'code', label: 'Code', required: true },
      { key: 'name', label: 'Name', required: true },
    ],
    list: (_criteria, page, size) => inventory.listWarehouses({ page, size }).pipe(map(pageData)),
    create: (value) =>
      inventory.createWarehouse({
        code: text(value['code']),
        name: text(value['name']),
      }),
    getById: (id) => inventory.getWarehouseById(id),
  };
}

export function inventoryItemsEntityConfig(): EntityConfig {
  const inventory = inject(InventoryService);
  return {
    key: 'inventory-items',
    title: 'Inventory items',
    description: 'On-hand, reserved, and available quantities by product and warehouse.',
    readonly: true,
    columns: [
      { key: 'warehouse.code', label: 'Warehouse Code' },
      { key: 'warehouse.name', label: 'Warehouse Name' },
      { key: 'product.sku', label: 'Product Sku' },
      { key: 'product.name', label: 'Product name' },
      { key: 'quantityOnHand', label: 'On hand' },
      { key: 'reservedQuantity', label: 'Reserved' },
      { key: 'availableQuantity', label: 'Available' },
    ],
    detailFields: [
      { key: 'id', label: 'Inventory item ID' },
      { key: 'productId', label: 'Product ID' },
      { key: 'warehouseId', label: 'Warehouse ID' },
      { key: 'quantityOnHand', label: 'On hand' },
      { key: 'reservedQuantity', label: 'Reserved' },
      { key: 'availableQuantity', label: 'Available' },
    ],
    searchFields: [
      { key: 'warehouseCode', label: 'Warehouse code' },
      { key: 'productSku', label: 'Product SKU' },
      { key: 'productName', label: 'Product name' },
    ],
    createFields: [],
    list: (criteria, page, size) =>
      inventory
        .listInventoryItems({
          ...criteria,
          page,
          size,
        } as ListInventoryItemsParams)
        .pipe(map(pageData)),
    getById: (id) => inventory.getInventoryItemById(id),
  };
}

export function stockMovementsEntityConfig(): EntityConfig {
  const inventory = inject(InventoryService);
  return {
    key: 'stock-movements',
    title: 'Stock movements',
    description: 'Inventory adjustments and movement reasons.',
    columns: [
      { key: 'productId', label: 'Product ID' },
      { key: 'warehouseId', label: 'Warehouse ID' },
      { key: 'quantity', label: 'Quantity' },
      { key: 'type', label: 'Type' },
      { key: 'reason', label: 'Reason' },
    ],
    detailFields: [
      { key: 'id', label: 'Movement ID' },
      { key: 'productId', label: 'Product ID' },
      { key: 'warehouseId', label: 'Warehouse ID' },
      { key: 'quantity', label: 'Quantity' },
      { key: 'type', label: 'Type' },
      { key: 'reason', label: 'Reason' },
    ],
    searchFields: [],
    createFields: [
      { key: 'productId', label: 'Product', required: true, type: 'select', optionsKey: 'products' },
      {
        key: 'warehouseId',
        label: 'Warehouse',
        required: true,
        type: 'select',
        optionsKey: 'warehouses',
      },
      { key: 'quantity', label: 'Quantity', required: true, type: 'number' },
      {
        key: 'type',
        label: 'Type',
        required: true,
        type: 'select',
        options: stockMovementTypeOptions,
      },
      {
        key: 'reason',
        label: 'Reason',
        required: true,
        type: 'select',
        options: stockMovementReasonOptions,
      },
    ],
    list: (_criteria, page, size) =>
      inventory.listStockMovements({ page, size }).pipe(map(pageData)),
    create: (value) =>
      inventory.createStockMovement({
        productId: text(value['productId']),
        warehouseId: text(value['warehouseId']),
        quantity: optionalNumber(value['quantity']),
        type: text(value['type']) as CreateStockMovementRequestType,
        reason: text(value['reason']) as CreateStockMovementRequestReason,
      }),
    getById: (id) => inventory.getStockMovementById(id),
  };
}

export function reservationsEntityConfig(): EntityConfig {
  const inventory = inject(InventoryService);
  return {
    key: 'reservations',
    title: 'Reservations',
    description: 'Inventory quantities reserved for sales orders.',
    columns: [
      { key: 'product.sku', label: 'Product SKU' },
      { key: 'warehouse.code', label: 'Warehouse ID' },
      { key: 'quantity', label: 'Quantity' },
      { key: 'salesOrder.orderNumber', label: 'Sales order number' },
    ],
    detailFields: [
      { key: 'id', label: 'Reservation ID' },
      { key: 'productId', label: 'Product ID' },
      { key: 'warehouseId', label: 'Warehouse ID' },
      { key: 'quantity', label: 'Quantity' },
      { key: 'salesOrderId', label: 'Sales order ID' },
    ],
    searchFields: [],
    createFields: [
      { key: 'productId', label: 'Product', required: true, type: 'select', optionsKey: 'products' },
      {
        key: 'warehouseId',
        label: 'Warehouse',
        required: true,
        type: 'select',
        optionsKey: 'warehouses',
      },
      { key: 'quantity', label: 'Quantity', required: true, type: 'number' },
      {
        key: 'salesOrderId',
        label: 'Sales order',
        required: true,
        type: 'select',
        optionsKey: 'salesOrders',
      },
    ],
    list: (_criteria, page, size) =>
      inventory.listInventoryReservations({ page, size }).pipe(map(pageData)),
    create: (value) =>
      inventory.createInventoryReservation({
        productId: text(value['productId']),
        warehouseId: text(value['warehouseId']),
        quantity: optionalNumber(value['quantity']),
        salesOrderId: text(value['salesOrderId']),
      }),
    getById: (id) => inventory.getInventoryReservationById(id),
  };
}

export const provideWarehousesEntityConfig = () =>
  provideEntityFeature({ config: warehousesEntityConfig, optionLoaders: [] });

export const provideInventoryItemsEntityConfig = () =>
  provideEntityFeature({ config: inventoryItemsEntityConfig, optionLoaders: [] });

export const provideStockMovementsEntityConfig = () =>
  provideEntityFeature({
    config: stockMovementsEntityConfig,
    optionLoaders: [ProductsOptionLoader, WarehousesOptionLoader],
  });

export const provideReservationsEntityConfig = () =>
  provideEntityFeature({
    config: reservationsEntityConfig,
    optionLoaders: [ProductsOptionLoader, WarehousesOptionLoader, SalesOrdersOptionLoader],
  });
