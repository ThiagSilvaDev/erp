package com.thiagsilvadev.erpapi.sales.internal.usecase;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.inventory.api.InventoryFacade;
import com.thiagsilvadev.erpapi.sales.api.OrderStatus;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesMapper;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesOrderResponse;
import com.thiagsilvadev.erpapi.sales.internal.event.SalesOrderEventPublisher;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;

@Component
public class CancelSalesOrderUseCase {

    private final SalesOrderRepository orderRepository;
    private final InventoryFacade inventoryFacade;
    private final SalesOrderEventPublisher eventPublisher;
    private final SalesMapper mapper;

    public CancelSalesOrderUseCase(
            SalesOrderRepository orderRepository,
            InventoryFacade inventoryFacade,
            SalesOrderEventPublisher eventPublisher,
            SalesMapper mapper) {
        this.orderRepository = orderRepository;
        this.inventoryFacade = inventoryFacade;
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.sales.api.SalesPermissions).WRITE)")
    public SalesOrderResponse execute(UUID id) {
        SalesOrder order =
                orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(SalesOrder.class, id));
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            eventPublisher.salesOrderCancelled(order);
        }
        order.cancel();
        inventoryFacade.releaseBySalesOrderId(order.getId());
        return mapper.toResponse(order);
    }
}
