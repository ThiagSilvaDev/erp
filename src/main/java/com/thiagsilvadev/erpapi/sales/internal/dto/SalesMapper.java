package com.thiagsilvadev.erpapi.sales.internal.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.thiagsilvadev.erpapi.common.domain.model.Money;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderDeliveryRouteSnapshot;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderItemSnapshot;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderSnapshot;
import com.thiagsilvadev.erpapi.sales.internal.model.*;

@Mapper(componentModel = "spring")
public interface SalesMapper {

    @Mapping(target = "totalAmount", expression = "java(order.totalAmount())")
    @Mapping(target = "currencyCode", expression = "java(order.currencyCode())")
    @Mapping(target = "formattedTotalAmount", expression = "java(formatTotalAmount(order))")
    @Mapping(target = "sellerSummary", ignore = true)
    @Mapping(target = "buyerSummary", ignore = true)
    SalesOrderResponse toResponse(SalesOrder order);

    @Mapping(target = "lineTotal", expression = "java(item.lineTotal())")
    SalesOrderItemResponse toResponse(SalesOrderItem item);

    default SalesOrderSnapshot toSnapshot(SalesOrder order) {
        if (order == null) {
            return null;
        }

        return new SalesOrderSnapshot(
                order.getId(),
                order.getOrderNumber(),
                order.getSaleType(),
                order.getFulfillmentType(),
                order.getPaymentMethod(),
                toDeliveryRouteSnapshot(order.getDeliveryRoute()),
                order.getSellerCompanyId(),
                order.getBuyerCompanyId(),
                order.getCustomerId(),
                order.getStatus(),
                order.totalAmount(),
                order.currencyCode(),
                order.getItems().stream()
                        .map(this::toItemSnapshot)
                        .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new)));
    }

    default SalesOrderDeliveryRouteSnapshot toDeliveryRouteSnapshot(DeliveryRoute deliveryRoute) {
        if (deliveryRoute == null) {
            return null;
        }

        return new SalesOrderDeliveryRouteSnapshot(
                deliveryRoute.getRouteName(),
                deliveryRoute.getOriginWarehouseId(),
                deliveryRoute.getDestinationDescription());
    }

    default SalesOrderItemSnapshot toItemSnapshot(SalesOrderItem item) {
        if (item == null) {
            return null;
        }

        return new SalesOrderItemSnapshot(
                item.getProductId(),
                item.getWarehouseId(),
                item.getProductSku(),
                item.getProductName(),
                item.getQuantity());
    }

    default String formatTotalAmount(SalesOrder order) {
        if (order == null) {
            return null;
        }

        return Money.of(order.totalAmount(), order.currencyCode()).getFormattedMoney();
    }
}
