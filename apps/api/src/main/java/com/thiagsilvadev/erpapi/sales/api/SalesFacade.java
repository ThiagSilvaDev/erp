package com.thiagsilvadev.erpapi.sales.api;

import java.util.List;
import java.util.UUID;

public interface SalesFacade {

    SalesOrderSnapshot getOrder(UUID sellerCompanyId, UUID salesOrderId);

    List<SalesOrderSnapshot> listOrders(UUID sellerCompanyId);
}
