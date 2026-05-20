package com.thiagsilvadev.erpapi.customer.internal.usecase;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.customer.internal.dto.CreateCustomerRequest;
import com.thiagsilvadev.erpapi.customer.internal.dto.CustomerMapper;
import com.thiagsilvadev.erpapi.customer.internal.dto.CustomerResponse;
import com.thiagsilvadev.erpapi.customer.internal.model.Customer;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerRepository;

@Component
public class CreateCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final CompanyFacade companyFacade;
    private final CustomerMapper mapper;

    public CreateCustomerUseCase(
            CustomerRepository customerRepository, CompanyFacade companyFacade, CustomerMapper mapper) {
        this.customerRepository = customerRepository;
        this.companyFacade = companyFacade;
        this.mapper = mapper;
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.customer.api.CustomerPermissions).WRITE)")
    public CustomerResponse execute(CreateCustomerRequest request) {
        companyFacade
                .findById(request.ownerCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException(Object.class, request.ownerCompanyId()));

        String document = Customer.normalizeDocument(request.document());
        if (customerRepository.existsByDocument(document)) {
            throw duplicateDocument(document);
        }

        Customer customer = Customer.create(
                request.name(),
                request.document(),
                request.ownerCompanyId(),
                mapper.toContact(request.contact()),
                mapper.toAddress(request.address()));

        try {
            return mapper.toResponse(customerRepository.saveAndFlush(customer));
        } catch (DataIntegrityViolationException e) {
            throw duplicateDocument(document);
        }
    }

    private static ConflictException duplicateDocument(String document) {
        return new ConflictException("Customer with this document already exists.", Map.of("document", document));
    }
}
