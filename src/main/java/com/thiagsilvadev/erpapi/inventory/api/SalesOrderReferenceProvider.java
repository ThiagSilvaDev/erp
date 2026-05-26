package com.thiagsilvadev.erpapi.inventory.api;

import java.util.Optional;
import java.util.UUID;

public interface SalesOrderReferenceProvider {

    Optional<SalesOrderReference> findSalesOrderReferenceById(UUID id);
}
