package com.thiagsilvadev.erpapi.sales.internal.usecase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.sales.internal.dto.SalesDtos.SalesOrderResponse;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesDtos.SalesOrderSearchCriteria;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesMapper;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderSpecification;

@Component
public class ListSalesOrdersUseCase {

    private final SalesOrderRepository orderRepository;
    private final SalesMapper mapper;

    public ListSalesOrdersUseCase(SalesOrderRepository orderRepository, SalesMapper mapper) {
        this.orderRepository = orderRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.sales.api.SalesPermissions).READ)")
    public Page<SalesOrderResponse> execute(SalesOrderSearchCriteria criteria, Pageable pageable) {
        return orderRepository
                .findAll(SalesOrderSpecification.withCriteria(criteria), pageable)
                .map(mapper::toResponse);
    }
}
