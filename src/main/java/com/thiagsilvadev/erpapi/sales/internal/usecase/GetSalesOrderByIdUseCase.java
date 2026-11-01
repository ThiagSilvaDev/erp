package com.thiagsilvadev.erpapi.sales.internal.usecase;

import static com.thiagsilvadev.erpapi.sales.api.SalesPermissions.READ;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
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
    @PreAuthorize("hasAuthority('" + READ + "')")
    public SalesOrderResponse execute(UUID id) {
        var sellerCompanyId = TenantContext.requireTenantId();
        return responseAssembler.toResponse(orderRepository
                .findByIdAndSellerCompanyId(id, sellerCompanyId)
                .orElseThrow(() -> new ResourceNotFoundException(SalesOrder.class, id)));
    }
}
