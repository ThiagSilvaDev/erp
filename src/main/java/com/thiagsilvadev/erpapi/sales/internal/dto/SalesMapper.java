package com.thiagsilvadev.erpapi.sales.internal.dto;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.sales.api.SalesOrderSnapshot;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesDtos.*;
import com.thiagsilvadev.erpapi.sales.internal.model.*;

@Component
public class SalesMapper {

    public SalesOrderResponse toResponse(SalesOrder order) {
        return new SalesOrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.totalAmount(),
                order.currencyCode(),
                order.getItems().stream().map(this::toResponse).toList());
    }

    public SalesOrderItemResponse toResponse(SalesOrderItem item) {
        return new SalesOrderItemResponse(
                item.getProductId(),
                item.getWarehouseId(),
                item.getProductSku(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPriceAmount(),
                item.getCurrencyCode(),
                item.lineTotal());
    }

    public SalesOrderSnapshot toSnapshot(SalesOrder order) {
        return new SalesOrderSnapshot(
                order.getId(), order.getCustomerId(), order.getStatus(), order.totalAmount(), order.currencyCode());
    }
}
