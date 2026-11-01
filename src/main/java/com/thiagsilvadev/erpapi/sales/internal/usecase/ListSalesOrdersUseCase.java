package com.thiagsilvadev.erpapi.sales.internal.usecase;

import static com.thiagsilvadev.erpapi.sales.api.SalesPermissions.READ;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesOrderResponse;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesOrderSearchCriteria;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderSpecification;

@Component
public class ListSalesOrdersUseCase {

    private final SalesOrderRepository orderRepository;
    private final SalesOrderResponseAssembler responseAssembler;

    public ListSalesOrdersUseCase(SalesOrderRepository orderRepository, SalesOrderResponseAssembler responseAssembler) {
        this.orderRepository = orderRepository;
        this.responseAssembler = responseAssembler;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<SalesOrderResponse> execute(SalesOrderSearchCriteria criteria, Pageable pageable) {
        var sellerCompanyId = TenantContext.requireTenantId();
        return orderRepository
                .findAll(SalesOrderSpecification.withCriteriaForSeller(criteria, sellerCompanyId), pageable)
                .map(responseAssembler::toResponse);
    }
}
