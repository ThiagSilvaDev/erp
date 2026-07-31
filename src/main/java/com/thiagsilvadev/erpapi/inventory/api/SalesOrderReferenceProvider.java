package com.thiagsilvadev.erpapi.inventory.api;

import java.util.UUID;

public interface SalesOrderReferenceProvider {

    SalesOrderReference getSalesOrderReference(UUID sellerCompanyId, UUID salesOrderId);
}
