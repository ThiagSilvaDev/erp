package com.thiagsilvadev.erpapi.customer.internal.usecase;

import static com.thiagsilvadev.erpapi.customer.api.CustomerPermissions.READ;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.customer.internal.dto.SupplierResponse;
import com.thiagsilvadev.erpapi.customer.internal.dto.SupplierSearchCriteria;
import com.thiagsilvadev.erpapi.customer.internal.mapper.CustomerMapper;
import com.thiagsilvadev.erpapi.customer.internal.model.Supplier;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerSpecification;
import com.thiagsilvadev.erpapi.customer.internal.repository.SupplierRepository;

@Component
public class SupplierQueryUseCase {

    private final SupplierRepository supplierRepository;
    private final CustomerMapper mapper;

    public SupplierQueryUseCase(SupplierRepository supplierRepository, CustomerMapper mapper) {
        this.supplierRepository = supplierRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public SupplierResponse getById(UUID id) {
        var companyId = TenantContext.requireTenantId();
        return mapper.toResponse(supplierRepository
                .findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(Supplier.class, id)));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<SupplierResponse> list(SupplierSearchCriteria criteria, Pageable pageable) {
        var companyId = TenantContext.requireTenantId();
        return supplierRepository
                .findAll(CustomerSpecification.suppliersForCompany(criteria, companyId), pageable)
                .map(mapper::toResponse);
    }
}
