package com.thiagsilvadev.erpapi.customer.internal.usecase;

import static com.thiagsilvadev.erpapi.customer.api.CustomerPermissions.READ;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.customer.internal.dto.SupplierResponse;
import com.thiagsilvadev.erpapi.customer.internal.mapper.CustomerMapper;
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
    @PreAuthorize("hasAuthority('" + READ + "')")
    public SupplierResponse execute(UUID id) {
        var companyId = TenantContext.requireTenantId();
        return mapper.toResponse(supplierRepository
                .findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(Supplier.class, id)));
    }
}
