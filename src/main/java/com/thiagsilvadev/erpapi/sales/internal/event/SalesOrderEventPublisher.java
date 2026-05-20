package com.thiagsilvadev.erpapi.sales.internal.event;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.sales.api.SalesOrderCancelledEvent;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderConfirmedEvent;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderDeliveryRequestedEvent;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;

@Component
public class SalesOrderEventPublisher {

    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public SalesOrderEventPublisher(ApplicationEventPublisher eventPublisher, Clock clock) {
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    public void salesOrderConfirmed(SalesOrder order) {
        eventPublisher.publishEvent(new SalesOrderConfirmedEvent(
                order.getId(),
                order.getSaleType(),
                order.getSellerCompanyId(),
                order.getBuyerCompanyId(),
                order.getCustomerId(),
                order.totalAmount(),
                order.currencyCode(),
                LocalDate.now(clock),
                order.getItems().stream()
                        .map(item -> new com.thiagsilvadev.erpapi.sales.api.SalesOrderConfirmedItem(
                                item.getProductId(),
                                item.getWarehouseId(),
                                item.getProductSku(),
                                item.getProductName(),
                                item.getQuantity()))
                        .toList()));
    }

    public void salesOrderDeliveryRequested(SalesOrder order) {
        eventPublisher.publishEvent(new SalesOrderDeliveryRequestedEvent(
                order.getId(),
                order.getSellerCompanyId(),
                order.getBuyerCompanyId(),
                order.getCustomerId(),
                order.getDeliveryRoute().getRouteName(),
                order.getDeliveryRoute().getOriginWarehouseId(),
                order.getDeliveryRoute().getDestinationDescription(),
                order.getItems().stream()
                        .map(item -> new SalesOrderDeliveryRequestedEvent.Item(
                                item.getProductId(),
                                item.getWarehouseId(),
                                item.getProductSku(),
                                item.getProductName(),
                                item.getQuantity()))
                        .toList(),
                clock.instant()));
    }

    public void salesOrderCancelled(SalesOrder order) {
        eventPublisher.publishEvent(new SalesOrderCancelledEvent(order.getId()));
    }
}
