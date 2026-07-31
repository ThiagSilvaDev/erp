package com.thiagsilvadev.erpapi.customer.api;

import java.util.UUID;

public interface CustomerFacade {

    CustomerSnapshot getCustomer(UUID ownerCompanyId, UUID customerId);

    CustomerSnapshot resolveCustomer(UUID ownerCompanyId, String document);
}
