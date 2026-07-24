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
        eventPublisher.publishEvent(new SalesOrderConfirmedEvent(order.getId(), LocalDate.now(clock)));
    }

    public void salesOrderDeliveryRequested(SalesOrder order) {
        eventPublisher.publishEvent(new SalesOrderDeliveryRequestedEvent(order.getId()));
    }

    public void salesOrderCancelled(SalesOrder order) {
        eventPublisher.publishEvent(new SalesOrderCancelledEvent(order.getId()));
    }
}
