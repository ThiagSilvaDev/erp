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
    private final SalesOrderQueryUseCase salesOrderQueryUseCase;
    private final ConfirmSalesOrderUseCase confirmSalesOrderUseCase;
    private final CancelSalesOrderUseCase cancelSalesOrderUseCase;

    SalesOrderController(
            CreateSalesOrderUseCase createSalesOrderUseCase,
            SalesOrderQueryUseCase salesOrderQueryUseCase,
            ConfirmSalesOrderUseCase confirmSalesOrderUseCase,
            CancelSalesOrderUseCase cancelSalesOrderUseCase) {
        this.createSalesOrderUseCase = createSalesOrderUseCase;
        this.salesOrderQueryUseCase = salesOrderQueryUseCase;
        this.confirmSalesOrderUseCase = confirmSalesOrderUseCase;
        this.cancelSalesOrderUseCase = cancelSalesOrderUseCase;
    }

    @Override
    public ResponseEntity<SalesOrderResponse> create(@Valid @RequestBody CreateSalesOrderRequest request) {
        SalesOrderResponse response = createSalesOrderUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public SalesOrderResponse getById(@PathVariable UUID id) {
        return salesOrderQueryUseCase.getById(id);
    }

    @Override
    public Page<SalesOrderResponse> list(SalesOrderSearchCriteria criteria, Pageable pageable) {
        return salesOrderQueryUseCase.list(criteria, pageable);
    }

    @Override
    public SalesOrderResponse confirm(@PathVariable UUID id) {
        return confirmSalesOrderUseCase.execute(id);
    }

    @Override
    public SalesOrderResponse cancel(@PathVariable UUID id) {
        return cancelSalesOrderUseCase.execute(id);
    }
}
