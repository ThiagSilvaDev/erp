package com.thiagsilvadev.erpapi.customer.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface CustomerFacade {

    CustomerSnapshot getCustomer(UUID ownerCompanyId, UUID customerId);

    List<CustomerSnapshot> getCustomers(UUID ownerCompanyId, Set<UUID> customerIds);

    CustomerSnapshot resolveCustomer(UUID ownerCompanyId, String document);
}
