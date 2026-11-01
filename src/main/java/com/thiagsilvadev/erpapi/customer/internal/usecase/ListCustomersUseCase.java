package com.thiagsilvadev.erpapi.customer.internal.usecase;

import static com.thiagsilvadev.erpapi.customer.api.CustomerPermissions.READ;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.customer.internal.dto.CustomerMapper;
import com.thiagsilvadev.erpapi.customer.internal.dto.CustomerResponse;
import com.thiagsilvadev.erpapi.customer.internal.dto.CustomerSearchCriteria;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerRepository;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerSpecification;

@Component
public class ListCustomersUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerMapper mapper;

    public ListCustomersUseCase(CustomerRepository customerRepository, CustomerMapper mapper) {
        this.customerRepository = customerRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<CustomerResponse> execute(CustomerSearchCriteria criteria, Pageable pageable) {
        var ownerCompanyId = TenantContext.requireTenantId();
        return customerRepository
                .findAll(CustomerSpecification.customersForCompany(criteria, ownerCompanyId), pageable)
                .map(mapper::toResponse);
    }
}
