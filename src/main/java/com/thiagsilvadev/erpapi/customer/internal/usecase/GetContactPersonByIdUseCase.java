package com.thiagsilvadev.erpapi.customer.internal.usecase;

import static com.thiagsilvadev.erpapi.customer.api.CustomerPermissions.READ;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.customer.internal.dto.ContactPersonResponse;
import com.thiagsilvadev.erpapi.customer.internal.mapper.CustomerMapper;
import com.thiagsilvadev.erpapi.customer.internal.model.ContactPerson;
import com.thiagsilvadev.erpapi.customer.internal.repository.ContactPersonRepository;

@Component
public class GetContactPersonByIdUseCase {

    private final ContactPersonRepository contactPersonRepository;
    private final CustomerMapper mapper;

    public GetContactPersonByIdUseCase(ContactPersonRepository contactPersonRepository, CustomerMapper mapper) {
        this.contactPersonRepository = contactPersonRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public ContactPersonResponse execute(UUID id) {
        var ownerCompanyId = TenantContext.requireTenantId();
        return mapper.toResponse(contactPersonRepository
                .findByIdAndCustomerOwnerCompanyId(id, ownerCompanyId)
                .orElseThrow(() -> new ResourceNotFoundException(ContactPerson.class, id)));
    }
}
