package com.thiagsilvadev.erpapi.sales.internal.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.thiagsilvadev.erpapi.common.domain.model.Money;
import com.thiagsilvadev.erpapi.sales.api.FulfillmentType;
import com.thiagsilvadev.erpapi.sales.api.PaymentMethod;
import com.thiagsilvadev.erpapi.sales.api.SaleType;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderStatus;
import com.thiagsilvadev.erpapi.sales.internal.dto.SalesOrderSearchCriteria;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrderItem;

/**
 * Persistence-level coverage for {@link SalesOrderSpecification}: buyer attributes live in the
 * embedded {@code SalesBuyer}, so criteria must resolve them through that path.
 */
@SpringBootTest
@ActiveProfiles("dbtest")
class SalesOrderSpecificationPersistenceTest {

    @Autowired
    private SalesOrderRepository orderRepository;

    private UUID persistedOrderId;

    @AfterEach
    void deletePersistedOrder() {
        if (persistedOrderId != null) {
            orderRepository.deleteById(persistedOrderId);
            persistedOrderId = null;
        }
    }

    @Test
    void shouldFilterByBuyerAttributesAndStatus() {
        UUID sellerCompanyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        persistedOrderId = persistDraftB2cOrder(sellerCompanyId, customerId).getId();

        var page = orderRepository.findAll(
                SalesOrderSpecification.withCriteriaForSeller(
                        new SalesOrderSearchCriteria(SaleType.B2C, null, null, customerId, SalesOrderStatus.DRAFT),
                        sellerCompanyId),
                PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(SalesOrder::getId).containsExactly(persistedOrderId);
    }

    @Test
    void shouldIgnoreOrderFromAnotherSeller() {
        UUID sellerCompanyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        persistedOrderId = persistDraftB2cOrder(sellerCompanyId, customerId).getId();

        var page = orderRepository.findAll(
                SalesOrderSpecification.withCriteriaForSeller(
                        new SalesOrderSearchCriteria(SaleType.B2C, null, null, customerId, SalesOrderStatus.DRAFT),
                        UUID.randomUUID()),
                PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
    }

    private SalesOrder persistDraftB2cOrder(UUID sellerCompanyId, UUID customerId) {
        return orderRepository.saveAndFlush(SalesOrder.createB2C(
                uniqueOrderNumber(),
                FulfillmentType.PICKUP,
                PaymentMethod.PIX,
                null,
                sellerCompanyId,
                customerId,
                Set.of(SalesOrderItem.create(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "SKU-SPEC-001",
                        "Specification product",
                        1,
                        Money.of(BigDecimal.TEN, "BRL")))));
    }

    private static long uniqueOrderNumber() {
        return 999_000_000L + Math.abs(UUID.randomUUID().getMostSignificantBits() % 1_000_000L);
    }
}
