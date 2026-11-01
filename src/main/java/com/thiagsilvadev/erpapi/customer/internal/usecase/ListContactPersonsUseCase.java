package com.thiagsilvadev.erpapi.customer.internal.usecase;

import static com.thiagsilvadev.erpapi.customer.api.CustomerPermissions.READ;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.customer.internal.dto.ContactPersonResponse;
import com.thiagsilvadev.erpapi.customer.internal.mapper.CustomerMapper;
import com.thiagsilvadev.erpapi.customer.internal.repository.ContactPersonRepository;

@Component
public class ListContactPersonsUseCase {

    private final ContactPersonRepository contactPersonRepository;
    private final CustomerMapper mapper;

    public ListContactPersonsUseCase(ContactPersonRepository contactPersonRepository, CustomerMapper mapper) {
        this.contactPersonRepository = contactPersonRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<ContactPersonResponse> execute(Pageable pageable) {
        var ownerCompanyId = TenantContext.requireTenantId();
        return contactPersonRepository
                .findAllByCustomerOwnerCompanyId(ownerCompanyId, pageable)
                .map(mapper::toResponse);
    }
}
