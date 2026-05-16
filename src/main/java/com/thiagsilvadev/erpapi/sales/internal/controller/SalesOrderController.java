package com.thiagsilvadev.erpapi.sales.internal.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.inventory.api.InventoryFacade;
import com.thiagsilvadev.erpapi.sales.api.*;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesDtos.*;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesMapper;
import com.thiagsilvadev.erpapi.sales.internal.model.*;
import com.thiagsilvadev.erpapi.sales.internal.repository.*;

@RestController
class SalesOrderController implements SalesOrderApi, SalesFacade {

    private final SalesOrderRepository orderRepository;
    private final CustomerFacade customerFacade;
    private final CatalogFacade catalogFacade;
    private final InventoryFacade inventoryFacade;
    private final SalesMapper mapper;

    SalesOrderController(
            SalesOrderRepository orderRepository,
            CustomerFacade customerFacade,
            CatalogFacade catalogFacade,
            InventoryFacade inventoryFacade,
            SalesMapper mapper) {
        this.orderRepository = orderRepository;
        this.customerFacade = customerFacade;
        this.catalogFacade = catalogFacade;
        this.inventoryFacade = inventoryFacade;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ResponseEntity<SalesOrderResponse> createOrder(@Valid @RequestBody CreateSalesOrderRequest request) {
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
        SalesOrderResponse response =
                mapper.toResponse(orderRepository.save(SalesOrder.create(request.customerId(), items)));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(response);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<SalesOrderResponse> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(
                orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(SalesOrder.class, id))));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Page<SalesOrderResponse>> listOrders(SalesOrderSearchCriteria criteria, Pageable pageable) {
        return ResponseEntity.ok(orderRepository
                .findAll(SalesOrderSpecification.withCriteria(criteria), pageable)
                .map(mapper::toResponse));
    }

    @Override
    @Transactional
    public ResponseEntity<SalesOrderResponse> confirmOrder(@PathVariable UUID id) {
        SalesOrder order =
                orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(SalesOrder.class, id));
        order.confirm();
        for (SalesOrderItem item : order.getItems()) {
            inventoryFacade.reserve(item.getProductId(), item.getWarehouseId(), item.getQuantity(), order.getId());
        }
        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @Override
    @Transactional
    public ResponseEntity<SalesOrderResponse> cancelOrder(@PathVariable UUID id) {
        SalesOrder order =
                orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(SalesOrder.class, id));
        order.cancel();
        inventoryFacade.releaseBySalesOrderId(order.getId());
        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<SalesOrderSnapshot> findOrderById(UUID id) {
        return orderRepository.findById(id).map(mapper::toSnapshot);
    }
}
