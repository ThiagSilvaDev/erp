package com.thiagsilvadev.erpapi.customer.internal.usecase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.customer.internal.dto.ContactPersonResponse;
import com.thiagsilvadev.erpapi.customer.internal.dto.CustomerMapper;
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
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.customer.api.CustomerPermissions).READ)")
    public Page<ContactPersonResponse> execute(Pageable pageable) {
        return contactPersonRepository.findAll(pageable).map(mapper::toResponse);
    }
}
