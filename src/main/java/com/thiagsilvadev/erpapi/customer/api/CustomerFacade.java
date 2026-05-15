package com.thiagsilvadev.erpapi.customer.api;

import java.util.Optional;
import java.util.UUID;

public interface CustomerFacade {

    Optional<CustomerSnapshot> findCustomerById(UUID id);

    Optional<CustomerSnapshot> findCustomerByDocument(String document);

    Optional<SupplierSnapshot> findSupplierById(UUID id);
}
