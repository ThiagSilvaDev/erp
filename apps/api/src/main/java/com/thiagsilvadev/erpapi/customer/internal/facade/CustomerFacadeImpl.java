package com.thiagsilvadev.erpapi.customer.internal.facade;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.customer.api.CustomerSnapshot;
import com.thiagsilvadev.erpapi.customer.internal.mapper.CustomerMapper;
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
    public List<CustomerSnapshot> getCustomers(UUID ownerCompanyId, Set<UUID> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            return List.of();
        }

        List<Customer> customers = customerRepository.findAllByOwnerCompanyIdAndIdIn(ownerCompanyId, customerIds);
        Set<UUID> missingIds = new TreeSet<>(customerIds);
        customers.forEach(customer -> missingIds.remove(customer.getId()));
        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException(Customer.class, missingIds);
        }

        return customers.stream().map(customerMapper::toSnapshot).toList();
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
