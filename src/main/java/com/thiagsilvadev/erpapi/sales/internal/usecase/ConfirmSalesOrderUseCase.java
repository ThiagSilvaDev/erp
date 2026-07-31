package com.thiagsilvadev.erpapi.sales.internal.usecase;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.inventory.api.InventoryFacade;
import com.thiagsilvadev.erpapi.inventory.api.ReserveStockCommand;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesOrderResponse;
import com.thiagsilvadev.erpapi.sales.internal.event.SalesOrderEventPublisher;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrderItem;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;

@Component
public class ConfirmSalesOrderUseCase {

    private final SalesOrderRepository orderRepository;
    private final InventoryFacade inventoryFacade;
    private final SalesOrderEventPublisher eventPublisher;
    private final SalesOrderResponseAssembler responseAssembler;

    public ConfirmSalesOrderUseCase(
            SalesOrderRepository orderRepository,
            InventoryFacade inventoryFacade,
            SalesOrderEventPublisher eventPublisher,
            SalesOrderResponseAssembler responseAssembler) {
        this.orderRepository = orderRepository;
        this.inventoryFacade = inventoryFacade;
        this.eventPublisher = eventPublisher;
        this.responseAssembler = responseAssembler;
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.sales.api.SalesPermissions).WRITE)")
    public SalesOrderResponse execute(UUID id) {
        var sellerCompanyId = TenantContext.requireTenantId();
        SalesOrder order = orderRepository
                .findByIdAndSellerCompanyId(id, sellerCompanyId)
                .orElseThrow(() -> new ResourceNotFoundException(SalesOrder.class, id));
        order.confirm();
        for (SalesOrderItem item : order.getItems()) {
            inventoryFacade.reserveStock(new ReserveStockCommand(
                    sellerCompanyId, item.getProductId(), item.getWarehouseId(), item.getQuantity(), order.getId()));
        }
        eventPublisher.salesOrderConfirmed(order);

        if (order.isDelivery()) {
            eventPublisher.salesOrderDeliveryRequested(order);
        }

        return responseAssembler.toResponse(order);
    }
}
