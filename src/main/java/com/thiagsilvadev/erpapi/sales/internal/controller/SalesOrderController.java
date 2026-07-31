package com.thiagsilvadev.erpapi.sales.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.thiagsilvadev.erpapi.common.web.ResponseEntities;
import com.thiagsilvadev.erpapi.sales.internal.dto.*;
import com.thiagsilvadev.erpapi.sales.internal.usecase.*;

@RestController
class SalesOrderController implements SalesOrderApi {

    private final CreateSalesOrderUseCase createSalesOrderUseCase;
    private final GetSalesOrderByIdUseCase getSalesOrderByIdUseCase;
    private final ListSalesOrdersUseCase listSalesOrdersUseCase;
    private final ConfirmSalesOrderUseCase confirmSalesOrderUseCase;
    private final CancelSalesOrderUseCase cancelSalesOrderUseCase;

    SalesOrderController(
            CreateSalesOrderUseCase createSalesOrderUseCase,
            GetSalesOrderByIdUseCase getSalesOrderByIdUseCase,
            ListSalesOrdersUseCase listSalesOrdersUseCase,
            ConfirmSalesOrderUseCase confirmSalesOrderUseCase,
            CancelSalesOrderUseCase cancelSalesOrderUseCase) {
        this.createSalesOrderUseCase = createSalesOrderUseCase;
        this.getSalesOrderByIdUseCase = getSalesOrderByIdUseCase;
        this.listSalesOrdersUseCase = listSalesOrdersUseCase;
        this.confirmSalesOrderUseCase = confirmSalesOrderUseCase;
        this.cancelSalesOrderUseCase = cancelSalesOrderUseCase;
    }

    @Override
    public ResponseEntity<SalesOrderResponse> createOrder(@Valid @RequestBody CreateSalesOrderRequest request) {
        SalesOrderResponse response = createSalesOrderUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public SalesOrderResponse getOrder(@PathVariable UUID id) {
        return getSalesOrderByIdUseCase.execute(id);
    }

    @Override
    public Page<SalesOrderResponse> listOrders(SalesOrderSearchCriteria criteria, Pageable pageable) {
        return listSalesOrdersUseCase.execute(criteria, pageable);
    }

    @Override
    public SalesOrderResponse confirmOrder(@PathVariable UUID id) {
        return confirmSalesOrderUseCase.execute(id);
    }

    @Override
    public SalesOrderResponse cancelOrder(@PathVariable UUID id) {
        return cancelSalesOrderUseCase.execute(id);
    }
}
