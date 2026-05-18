package com.thiagsilvadev.erpapi.sales.internal.usecase;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.inventory.api.InventoryFacade;
import com.thiagsilvadev.erpapi.sales.internal.event.SalesOrderEventPublisher;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesDtos.SalesOrderResponse;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesMapper;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrderItem;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;

@Component
public class ConfirmSalesOrderUseCase {

    private final SalesOrderRepository orderRepository;
    private final InventoryFacade inventoryFacade;
    private final SalesOrderEventPublisher eventPublisher;
    private final SalesMapper mapper;

    public ConfirmSalesOrderUseCase(
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
        order.confirm();
        for (SalesOrderItem item : order.getItems()) {
            inventoryFacade.reserve(item.getProductId(), item.getWarehouseId(), item.getQuantity(), order.getId());
        }
        eventPublisher.salesOrderConfirmed(order);
        return mapper.toResponse(order);
    }
}
