package com.thiagsilvadev.erpapi.sales.internal.seed;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.inventory.api.InventoryFacade;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrderItem;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;

@Component
@Profile("dev")
@Order(600)
class SalesDevSeeder implements ApplicationRunner {

    private static final String CUSTOMER_DOCUMENT = "12345678901";
    private static final String PRODUCT_SKU = "SKU-001";
    private static final String WAREHOUSE_CODE = "MAIN";

    private final SalesOrderRepository orderRepository;
    private final CustomerFacade customerFacade;
    private final CatalogFacade catalogFacade;
    private final InventoryFacade inventoryFacade;
    private final BusinessNumberService businessNumberService;

    SalesDevSeeder(
            SalesOrderRepository orderRepository,
            CustomerFacade customerFacade,
            CatalogFacade catalogFacade,
            InventoryFacade inventoryFacade,
            BusinessNumberService businessNumberService) {
        this.orderRepository = orderRepository;
        this.customerFacade = customerFacade;
        this.catalogFacade = catalogFacade;
        this.inventoryFacade = inventoryFacade;
        this.businessNumberService = businessNumberService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (orderRepository.count() > 0) {
            return;
        }
        var customer = customerFacade
                .findCustomerByDocument(CUSTOMER_DOCUMENT)
                .orElseThrow(() -> new IllegalStateException("Seed customer must exist before seeding sales"));
        var product = catalogFacade
                .findProductBySku(PRODUCT_SKU)
                .orElseThrow(() -> new IllegalStateException("Seed product must exist before seeding sales"));
        var warehouse = inventoryFacade
                .findWarehouseByCode(WAREHOUSE_CODE)
                .orElseThrow(() -> new IllegalStateException("Seed warehouse must exist before seeding sales"));
        orderRepository.save(SalesOrder.create(
                businessNumberService.next(BusinessNumberType.SALES_ORDER),
                customer.id(),
                List.of(SalesOrderItem.create(
                        product.id(),
                        warehouse.id(),
                        product.sku(),
                        product.name(),
                        2,
                        product.basePrice().getAmount(),
                        product.basePrice().getCurrencyCode()))));
    }
}
