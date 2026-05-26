package com.thiagsilvadev.erpapi.logistics.internal.listener;

import java.time.Clock;
import java.time.Instant;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
import com.thiagsilvadev.erpapi.logistics.internal.model.*;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentRepository;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentTrackingEventRepository;
import com.thiagsilvadev.erpapi.sales.api.SalesFacade;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderCancelledEvent;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderDeliveryRequestedEvent;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderSnapshot;

@Component
public class SalesOrderLogisticsListener {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackingEventRepository trackingEventRepository;
    private final BusinessNumberService businessNumberService;
    private final Clock clock;
    private final SalesFacade salesFacade;

    public SalesOrderLogisticsListener(
            ShipmentRepository shipmentRepository,
            ShipmentTrackingEventRepository trackingEventRepository,
            BusinessNumberService businessNumberService,
            Clock clock,
            SalesFacade salesFacade) {
        this.shipmentRepository = shipmentRepository;
        this.trackingEventRepository = trackingEventRepository;
        this.businessNumberService = businessNumberService;
        this.clock = clock;
        this.salesFacade = salesFacade;
    }

    @ApplicationModuleListener
    public void on(SalesOrderDeliveryRequestedEvent event) {
        if (shipmentRepository.existsBySalesOrderId(event.salesOrderId())) {
            return;
        }

        SalesOrderSnapshot order = salesFacade
                .findOrderById(event.salesOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", event.salesOrderId()));

        Shipment shipment = Shipment.create(
                businessNumberService.next(BusinessNumberType.SHIPMENT),
                event.salesOrderId(),
                order.sellerCompanyId(),
                order.buyerCompanyId(),
                order.customerId(),
                order.items().stream()
                        .map(item -> ShipmentItem.create(
                                item.productId(),
                                item.warehouseId(),
                                item.productSku(),
                                item.productName(),
                                item.quantity()))
                        .toList());

        shipment.assignRoute(
                order.deliveryRoute().routeName(),
                order.deliveryRoute().originWarehouseId(),
                order.deliveryRoute().destinationDescription());
        shipment = shipmentRepository.save(shipment);

        trackingEventRepository.save(ShipmentTrackingEvent.create(
                shipment,
                ShipmentStatus.CREATED,
                Instant.now(clock),
                null,
                "Shipment created from delivery sales order"));
    }

    @ApplicationModuleListener
    public void on(SalesOrderCancelledEvent event) {
        shipmentRepository.findBySalesOrderId(event.salesOrderId()).ifPresent(shipment -> {
            try {
                shipment.ensureBeforeLoading();
                Instant now = Instant.now(clock);
                shipment.cancel(now);
                trackingEventRepository.save(ShipmentTrackingEvent.create(
                        shipment, ShipmentStatus.CANCELLED, now, null, "Sales order cancelled"));
            } catch (IllegalStateException e) {
                throw new ConflictException("Cannot cancel sales order after shipment loading.");
            }
        });
    }
}
