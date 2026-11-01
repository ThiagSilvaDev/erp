package com.thiagsilvadev.erpapi.customer.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.thiagsilvadev.erpapi.common.web.ResponseEntities;
import com.thiagsilvadev.erpapi.customer.internal.dto.*;
import com.thiagsilvadev.erpapi.customer.internal.usecase.*;

@RestController
class SupplierController implements SupplierApi {

    private final CreateSupplierUseCase createSupplierUseCase;
    private final SupplierQueryUseCase supplierQueryUseCase;

    SupplierController(CreateSupplierUseCase createSupplierUseCase, SupplierQueryUseCase supplierQueryUseCase) {
        this.createSupplierUseCase = createSupplierUseCase;
        this.supplierQueryUseCase = supplierQueryUseCase;
    }

    @Override
    public ResponseEntity<SupplierResponse> createSupplier(@Valid CreateSupplierRequest request) {
        SupplierResponse response = createSupplierUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public SupplierResponse getSupplierById(UUID id) {
        return supplierQueryUseCase.getById(id);
    }

    @Override
    public Page<SupplierResponse> listSuppliers(SupplierSearchCriteria criteria, Pageable pageable) {
        return supplierQueryUseCase.list(criteria, pageable);
    }
}
