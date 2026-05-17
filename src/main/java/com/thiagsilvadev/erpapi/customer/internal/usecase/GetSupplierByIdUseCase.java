package com.thiagsilvadev.erpapi.customer.internal.usecase;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.customer.internal.dto.CustomerMapper;
import com.thiagsilvadev.erpapi.customer.internal.dto.SupplierResponse;
import com.thiagsilvadev.erpapi.customer.internal.model.Supplier;
import com.thiagsilvadev.erpapi.customer.internal.repository.SupplierRepository;

@Component
public class GetSupplierByIdUseCase {

    private final SupplierRepository supplierRepository;
    private final CustomerMapper mapper;

    public GetSupplierByIdUseCase(SupplierRepository supplierRepository, CustomerMapper mapper) {
        this.supplierRepository = supplierRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.customer.api.CustomerPermissions).READ)")
    public SupplierResponse execute(UUID id) {
        return mapper.toResponse(
                supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Supplier.class, id)));
    }
}
