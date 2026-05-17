package com.thiagsilvadev.erpapi.sales.internal.usecase;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesDtos.SalesOrderResponse;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesMapper;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;

@Component
public class GetSalesOrderByIdUseCase {

    private final SalesOrderRepository orderRepository;
    private final SalesMapper mapper;

    public GetSalesOrderByIdUseCase(SalesOrderRepository orderRepository, SalesMapper mapper) {
        this.orderRepository = orderRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public SalesOrderResponse execute(UUID id) {
        return mapper.toResponse(
                orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(SalesOrder.class, id)));
    }
}
