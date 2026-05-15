package com.thiagsilvadev.erpapi.sales.api;

import java.util.Optional;
import java.util.UUID;

public interface SalesFacade {

    Optional<SalesOrderSnapshot> findOrderById(UUID id);
}
