package com.thiagsilvadev.erpapi.customer.internal.usecase;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.customer.internal.dto.ContactPersonResponse;
import com.thiagsilvadev.erpapi.customer.internal.dto.CreateContactPersonRequest;
import com.thiagsilvadev.erpapi.customer.internal.dto.CustomerMapper;
import com.thiagsilvadev.erpapi.customer.internal.model.ContactPerson;
import com.thiagsilvadev.erpapi.customer.internal.model.Customer;
import com.thiagsilvadev.erpapi.customer.internal.repository.ContactPersonRepository;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerRepository;

@Component
public class CreateContactPersonUseCase {

    private final ContactPersonRepository contactPersonRepository;
    private final CustomerRepository customerRepository;
    private final CustomerMapper mapper;

    public CreateContactPersonUseCase(
            ContactPersonRepository contactPersonRepository,
            CustomerRepository customerRepository,
            CustomerMapper mapper) {
        this.contactPersonRepository = contactPersonRepository;
        this.customerRepository = customerRepository;
        this.mapper = mapper;
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.customer.api.CustomerPermissions).WRITE)")
    public ContactPersonResponse execute(CreateContactPersonRequest request) {
        var ownerCompanyId = TenantContext.requireTenantId();
        customerRepository
                .findByIdAndOwnerCompanyId(request.customerId(), ownerCompanyId)
                .orElseThrow(() -> new ResourceNotFoundException(Customer.class, request.customerId()));

        ContactPerson contactPerson =
                ContactPerson.create(request.name(), request.customerId(), mapper.toContact(request.contact()));

        return mapper.toResponse(contactPersonRepository.save(contactPerson));
    }
}
