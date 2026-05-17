package com.thiagsilvadev.erpapi.customer.internal.usecase;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.customer.internal.dto.CreateSupplierRequest;
import com.thiagsilvadev.erpapi.customer.internal.dto.CustomerMapper;
import com.thiagsilvadev.erpapi.customer.internal.dto.SupplierResponse;
import com.thiagsilvadev.erpapi.customer.internal.model.Supplier;
import com.thiagsilvadev.erpapi.customer.internal.repository.SupplierRepository;

@Component
public class CreateSupplierUseCase {

    private final SupplierRepository supplierRepository;
    private final CompanyFacade companyFacade;
    private final CustomerMapper mapper;

    public CreateSupplierUseCase(
            SupplierRepository supplierRepository, CompanyFacade companyFacade, CustomerMapper mapper) {
        this.supplierRepository = supplierRepository;
        this.companyFacade = companyFacade;
        this.mapper = mapper;
    }

    @Transactional
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.customer.api.CustomerPermissions).WRITE)")
    public SupplierResponse execute(CreateSupplierRequest request) {
        companyFacade
                .findById(request.companyId())
                .orElseThrow(() -> new ResourceNotFoundException(Object.class, request.companyId()));

        String document = Supplier.normalizeDocument(request.document());
        if (supplierRepository.existsByDocument(document)) {
            throw duplicateDocument(document);
        }

        Supplier supplier = Supplier.create(
                request.name(),
                request.document(),
                request.companyId(),
                mapper.toContact(request.contact()),
                mapper.toAddress(request.address()));

        try {
            return mapper.toResponse(supplierRepository.saveAndFlush(supplier));
        } catch (DataIntegrityViolationException e) {
            throw duplicateDocument(document);
        }
    }

    private static ConflictException duplicateDocument(String document) {
        return new ConflictException("Supplier with this document already exists.", Map.of("document", document));
    }
}
