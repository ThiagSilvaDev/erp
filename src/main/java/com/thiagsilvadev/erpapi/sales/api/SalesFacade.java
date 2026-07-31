package com.thiagsilvadev.erpapi.sales.api;

import java.util.UUID;

public interface SalesFacade {

    SalesOrderSnapshot getOrder(UUID sellerCompanyId, UUID salesOrderId);
}
