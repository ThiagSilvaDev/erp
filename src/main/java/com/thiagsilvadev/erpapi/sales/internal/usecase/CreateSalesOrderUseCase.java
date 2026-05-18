package com.thiagsilvadev.erpapi.sales.internal.usecase;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.sales.internal.dto.CreateSalesOrderRequest;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesMapper;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesOrderResponse;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrderItem;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;

@Component
public class CreateSalesOrderUseCase {

    private final SalesOrderRepository orderRepository;
    private final CustomerFacade customerFacade;
    private final CatalogFacade catalogFacade;
    private final SalesMapper mapper;

    public CreateSalesOrderUseCase(
            SalesOrderRepository orderRepository,
            CustomerFacade customerFacade,
            CatalogFacade catalogFacade,
            SalesMapper mapper) {
        this.orderRepository = orderRepository;
        this.customerFacade = customerFacade;
        this.catalogFacade = catalogFacade;
        this.mapper = mapper;
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.sales.api.SalesPermissions).WRITE)")
    public SalesOrderResponse execute(CreateSalesOrderRequest request) {
        customerFacade
                .findCustomerById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException(Object.class, request.customerId()));
        var items = request.items().stream()
                .map(item -> {
                    var product = catalogFacade
                            .findProductById(item.productId())
                            .orElseThrow(() -> new ResourceNotFoundException(Object.class, item.productId()));
                    return SalesOrderItem.create(
                            product.id(),
                            item.warehouseId(),
                            product.sku(),
                            product.name(),
                            item.quantity(),
                            product.basePrice().getAmount(),
                            product.basePrice().getCurrencyCode());
                })
                .toList();
        return mapper.toResponse(orderRepository.save(SalesOrder.create(request.customerId(), items)));
    }
}
