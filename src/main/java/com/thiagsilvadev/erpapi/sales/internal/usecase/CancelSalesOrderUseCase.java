package com.thiagsilvadev.erpapi.sales.internal.usecase;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.inventory.api.InventoryFacade;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesDtos.SalesOrderResponse;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesMapper;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;

@Component
public class CancelSalesOrderUseCase {

    private final SalesOrderRepository orderRepository;
    private final InventoryFacade inventoryFacade;
    private final SalesMapper mapper;

    public CancelSalesOrderUseCase(
            SalesOrderRepository orderRepository, InventoryFacade inventoryFacade, SalesMapper mapper) {
        this.orderRepository = orderRepository;
        this.inventoryFacade = inventoryFacade;
        this.mapper = mapper;
    }

    @Transactional
    public SalesOrderResponse execute(UUID id) {
        SalesOrder order =
                orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(SalesOrder.class, id));
        order.cancel();
        inventoryFacade.releaseBySalesOrderId(order.getId());
        return mapper.toResponse(order);
    }
}
