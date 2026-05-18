package com.thiagsilvadev.erpapi.customer.internal.usecase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.customer.internal.dto.CustomerMapper;
import com.thiagsilvadev.erpapi.customer.internal.dto.CustomerSearchCriteria;
import com.thiagsilvadev.erpapi.customer.internal.dto.SupplierResponse;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerSpecification;
import com.thiagsilvadev.erpapi.customer.internal.repository.SupplierRepository;

@Component
public class ListSuppliersUseCase {

    private final SupplierRepository supplierRepository;
    private final CustomerMapper mapper;

    public ListSuppliersUseCase(SupplierRepository supplierRepository, CustomerMapper mapper) {
        this.supplierRepository = supplierRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.customer.api.CustomerPermissions).READ)")
    public Page<SupplierResponse> execute(CustomerSearchCriteria criteria, Pageable pageable) {
        return supplierRepository
                .findAll(CustomerSpecification.suppliers(criteria), pageable)
                .map(mapper::toResponse);
    }
}
