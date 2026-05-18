package com.thiagsilvadev.erpapi.sales.internal.usecase;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.catalog.api.ProductSnapshot;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.sales.internal.dto.CreateSalesOrderItemRequest;
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
        Set<UUID> productIds = request.items().stream()
                .map(CreateSalesOrderItemRequest::productId)
                .collect(Collectors.toSet());

        Map<UUID, ProductSnapshot> productMap = catalogFacade.findAllProductsByIdIn(productIds).stream()
                .collect(Collectors.toMap(ProductSnapshot::id, p -> p));

        var items = request.items().stream()
                .map(item -> {
                    ProductSnapshot product = Optional.ofNullable(productMap.get(item.productId()))
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
