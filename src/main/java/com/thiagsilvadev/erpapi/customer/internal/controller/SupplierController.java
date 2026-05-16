package com.thiagsilvadev.erpapi.customer.internal.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.customer.internal.dto.*;
import com.thiagsilvadev.erpapi.customer.internal.model.Supplier;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerSpecification;
import com.thiagsilvadev.erpapi.customer.internal.repository.SupplierRepository;

@RestController
class SupplierController implements SupplierApi {

    private final SupplierRepository supplierRepository;
    private final CompanyFacade companyFacade;
    private final CustomerMapper mapper;

    SupplierController(SupplierRepository supplierRepository, CompanyFacade companyFacade, CustomerMapper mapper) {
        this.supplierRepository = supplierRepository;
        this.companyFacade = companyFacade;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ResponseEntity<SupplierResponse> createSupplier(@Valid CreateSupplierRequest request) {
        companyFacade
                .findById(request.companyId())
                .orElseThrow(() -> new ResourceNotFoundException(Object.class, request.companyId()));
        String document = Supplier.normalizeDocument(request.document());
        if (supplierRepository.existsByDocument(document)) {
            throw new ConflictException(
                    "Supplier with this document already exists.", java.util.Map.of("document", document));
        }
        SupplierResponse response = mapper.toResponse(supplierRepository.save(mapper.toSupplier(request)));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(response);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<SupplierResponse> getSupplierById(UUID id) {
        return ResponseEntity.ok(mapper.toResponse(
                supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Supplier.class, id))));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Page<SupplierResponse>> listSuppliers(CustomerSearchCriteria criteria, Pageable pageable) {
        return ResponseEntity.ok(supplierRepository
                .findAll(CustomerSpecification.suppliers(criteria), pageable)
                .map(mapper::toResponse));
    }
}
