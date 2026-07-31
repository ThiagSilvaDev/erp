package com.thiagsilvadev.erpapi.customer.internal.facade;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.customer.api.CustomerSnapshot;
import com.thiagsilvadev.erpapi.customer.internal.dto.CustomerMapper;
import com.thiagsilvadev.erpapi.customer.internal.model.Customer;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerRepository;

@Service
public class CustomerFacadeImpl implements CustomerFacade {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerFacadeImpl(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerSnapshot getCustomer(UUID ownerCompanyId, UUID customerId) {
        return customerRepository
                .findByIdAndOwnerCompanyId(customerId, ownerCompanyId)
                .map(customerMapper::toSnapshot)
                .orElseThrow(() -> new ResourceNotFoundException(Customer.class, customerId));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerSnapshot resolveCustomer(UUID ownerCompanyId, String document) {
        String normalizedDocument = Customer.normalizeDocument(document);
        return customerRepository
                .findByOwnerCompanyIdAndDocument(ownerCompanyId, normalizedDocument)
                .map(customerMapper::toSnapshot)
                .orElseThrow(() -> new ResourceNotFoundException(Customer.class, document));
    }
}
