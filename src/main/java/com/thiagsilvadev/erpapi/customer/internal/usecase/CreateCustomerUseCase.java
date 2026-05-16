package com.thiagsilvadev.erpapi.customer.internal.usecase;

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
    public CustomerResponse execute(CreateCustomerRequest request) {
        companyFacade
                .findById(request.companyId())
                .orElseThrow(() -> new ResourceNotFoundException(Object.class, request.companyId()));

        String document = Customer.normalizeDocument(request.document());
        if (customerRepository.existsByDocument(document)) {
            throw new ConflictException(
                    "Customer with this document already exists.", java.util.Map.of("document", document));
        }

        Customer customer = Customer.create(
                request.name(),
                request.document(),
                request.companyId(),
                mapper.toContact(request.contact()),
                mapper.toAddress(request.address()));

        return mapper.toResponse(customerRepository.save(customer));
    }
}
