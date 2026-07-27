package com.thiagsilvadev.erpapi.sales.internal.seed;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.catalog.api.ProductSnapshot;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.inventory.api.InventoryFacade;
import com.thiagsilvadev.erpapi.sales.api.FulfillmentType;
import com.thiagsilvadev.erpapi.sales.api.OrderStatus;
import com.thiagsilvadev.erpapi.sales.api.PaymentMethod;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrderItem;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;

@Component
@Profile("dev")
@Order(600)
class SalesDevSeeder implements ApplicationRunner {

    private static final String CUSTOMER_DOCUMENT = "12345678901";
    private static final String WAREHOUSE_CODE = "MAIN";

    private static final List<OrderSeed> ORDERS = List.of(
            new OrderSeed(56, "SKU-002", 2, PaymentMethod.BANK_TRANSFER, OrderStatus.CONFIRMED),
            new OrderSeed(51, "SKU-003", 4, PaymentMethod.CREDIT_CARD, OrderStatus.CANCELLED),
            new OrderSeed(45, "SKU-004", 12, PaymentMethod.PIX, OrderStatus.CONFIRMED),
            new OrderSeed(38, "SKU-005", 6, PaymentMethod.BOLETO, OrderStatus.DRAFT),
            new OrderSeed(32, "SKU-001", 10, PaymentMethod.PIX, OrderStatus.CONFIRMED),
            new OrderSeed(27, "SKU-002", 3, PaymentMethod.BANK_TRANSFER, OrderStatus.CONFIRMED),
            new OrderSeed(23, "SKU-003", 5, PaymentMethod.CREDIT_CARD, OrderStatus.CONFIRMED),
            new OrderSeed(19, "SKU-004", 8, PaymentMethod.PIX, OrderStatus.CANCELLED),
            new OrderSeed(14, "SKU-005", 7, PaymentMethod.BOLETO, OrderStatus.CONFIRMED),
            new OrderSeed(9, "SKU-006", 10, PaymentMethod.DEBIT_CARD, OrderStatus.DRAFT),
            new OrderSeed(5, "SKU-001", 15, PaymentMethod.PIX, OrderStatus.CONFIRMED),
            new OrderSeed(1, "SKU-007", 20, PaymentMethod.CREDIT_CARD, OrderStatus.CANCELLED));

    private final SalesOrderRepository orderRepository;
    private final CustomerFacade customerFacade;
    private final CatalogFacade catalogFacade;
    private final InventoryFacade inventoryFacade;
    private final BusinessNumberService businessNumberService;
    private final Clock clock;

    SalesDevSeeder(
            SalesOrderRepository orderRepository,
            CustomerFacade customerFacade,
            CatalogFacade catalogFacade,
            InventoryFacade inventoryFacade,
            BusinessNumberService businessNumberService,
            Clock clock) {
        this.orderRepository = orderRepository;
        this.customerFacade = customerFacade;
        this.catalogFacade = catalogFacade;
        this.inventoryFacade = inventoryFacade;
        this.businessNumberService = businessNumberService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        var customer = customerFacade
                .findCustomerByDocument(CUSTOMER_DOCUMENT)
                .orElseThrow(() -> new IllegalStateException("Seed customer must exist before seeding sales"));
        UUID companyId = customer.ownerCompanyId();
        if (orderRepository.countBySellerCompanyId(companyId) >= ORDERS.size()) {
            return;
        }
        var warehouse = inventoryFacade
                .findWarehouseByCode(companyId, WAREHOUSE_CODE)
                .orElseThrow(() -> new IllegalStateException("Seed warehouse must exist before seeding sales"));
        LocalDate today = LocalDate.now(clock);

        for (OrderSeed seed : ORDERS) {
            ProductSnapshot product = catalogFacade
                    .findProductBySku(seed.sku(), companyId)
                    .orElseThrow(() -> new IllegalStateException("Seed product must exist before seeding sales"));
            SalesOrder order = SalesOrder.createB2C(
                    businessNumberService.next(BusinessNumberType.SALES_ORDER),
                    FulfillmentType.PICKUP,
                    seed.paymentMethod(),
                    null,
                    companyId,
                    customer.id(),
                    Set.of(SalesOrderItem.create(
                            product.id(),
                            warehouse.id(),
                            product.sku(),
                            product.name(),
                            seed.quantity(),
                            product.basePrice())));
            applyStatus(order, seed.status());
            orderRepository.saveAndFlush(order);
            orderRepository.updateCreatedAtForDevSeed(
                    order.getId(), today.minusDays(seed.daysAgo()).atTime(12, 0).toInstant(ZoneOffset.UTC));
        }
    }

    private static void applyStatus(SalesOrder order, OrderStatus status) {
        switch (status) {
            case DRAFT -> {
                // The factory already creates draft orders.
            }
            case CONFIRMED -> order.confirm();
            case CANCELLED -> order.cancel();
        }
    }

    static int seedOrderCount() {
        return ORDERS.size();
    }

    private record OrderSeed(int daysAgo, String sku, int quantity, PaymentMethod paymentMethod, OrderStatus status) {}
}
