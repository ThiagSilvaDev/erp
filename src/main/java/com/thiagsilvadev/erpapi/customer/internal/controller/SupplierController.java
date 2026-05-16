package com.thiagsilvadev.erpapi.customer.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.web.ResponseEntities;
import com.thiagsilvadev.erpapi.customer.internal.dto.*;
import com.thiagsilvadev.erpapi.customer.internal.model.Supplier;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerSpecification;
import com.thiagsilvadev.erpapi.customer.internal.repository.SupplierRepository;
import com.thiagsilvadev.erpapi.customer.internal.usecase.CreateSupplierUseCase;

@RestController
class SupplierController implements SupplierApi {

    private final SupplierRepository supplierRepository;
    private final CustomerMapper mapper;
    private final CreateSupplierUseCase createSupplierUseCase;

    SupplierController(
            SupplierRepository supplierRepository, CustomerMapper mapper, CreateSupplierUseCase createSupplierUseCase) {
        this.supplierRepository = supplierRepository;
        this.mapper = mapper;
        this.createSupplierUseCase = createSupplierUseCase;
    }

    @Override
    public ResponseEntity<SupplierResponse> createSupplier(@Valid CreateSupplierRequest request) {
        SupplierResponse response = createSupplierUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(UUID id) {
        return mapper.toResponse(
                supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Supplier.class, id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierResponse> listSuppliers(CustomerSearchCriteria criteria, Pageable pageable) {
        return supplierRepository
                .findAll(CustomerSpecification.suppliers(criteria), pageable)
                .map(mapper::toResponse);
    }
}
