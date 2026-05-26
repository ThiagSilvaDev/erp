package com.thiagsilvadev.erpapi.sales.internal.usecase;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesOrderResponse;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;

@Component
public class GetSalesOrderByIdUseCase {

    private final SalesOrderRepository orderRepository;
    private final SalesOrderResponseAssembler responseAssembler;

    public GetSalesOrderByIdUseCase(
            SalesOrderRepository orderRepository, SalesOrderResponseAssembler responseAssembler) {
        this.orderRepository = orderRepository;
        this.responseAssembler = responseAssembler;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.sales.api.SalesPermissions).READ)")
    public SalesOrderResponse execute(UUID id) {
        return responseAssembler.toResponse(
                orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(SalesOrder.class, id)));
    }
}
