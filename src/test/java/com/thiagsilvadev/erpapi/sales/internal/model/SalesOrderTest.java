package com.thiagsilvadev.erpapi.sales.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;
import com.thiagsilvadev.erpapi.common.domain.model.Money;
import com.thiagsilvadev.erpapi.sales.api.FulfillmentType;
import com.thiagsilvadev.erpapi.sales.api.PaymentMethod;
import com.thiagsilvadev.erpapi.sales.api.SaleType;
import com.thiagsilvadev.erpapi.sales.internal.exception.SalesOrderErrorCode;
import com.thiagsilvadev.erpapi.sales.internal.exception.SalesOrderException;
import com.thiagsilvadev.erpapi.sales.internal.exception.SalesOrderItemAssignmentException;
import com.thiagsilvadev.erpapi.sales.internal.exception.SalesOrderItemErrorCode;

class SalesOrderTest {

    @Test
    void shouldRemoveItemAndClearItemAssignment() {
        SalesOrderItem firstItem = item();
        SalesOrderItem secondItem = item();
        SalesOrder order = order(firstItem, secondItem);

        order.removeItem(firstItem);

        assertThat(order.getItems()).containsExactly(secondItem);
        assertThat(firstItem.getSalesOrder()).isNull();
        assertThat(secondItem.getSalesOrder()).isSameAs(order);
    }

    @Test
    void shouldRejectItemRemovalWhenOrderIsNotDraft() {
        SalesOrderItem item = item();
        SalesOrder order = order(item);
        order.confirm();

        assertThatThrownBy(() -> order.removeItem(item))
                .isInstanceOf(SalesOrderException.class)
                .satisfies(exception -> assertThat(((SalesOrderException) exception).getErrorCode())
                        .isEqualTo(SalesOrderErrorCode.MUST_BE_DRAFT_TO_REMOVE_ITEMS));
    }

    @Test
    void shouldRejectOrderWithoutItems() {
        LinkedHashSet<SalesOrderItem> orderItems = new LinkedHashSet<>();
        UUID sellerCompanyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        assertThatThrownBy(() -> SalesOrder.createB2C(
                        100L, FulfillmentType.PICKUP, PaymentMethod.PIX, null, sellerCompanyId, customerId, orderItems))
                .isInstanceOf(SalesOrderException.class)
                .satisfies(exception -> assertThat(((SalesOrderException) exception).getErrorCode())
                        .isEqualTo(SalesOrderErrorCode.ITEMS_REQUIRED));
    }

    @Test
    void shouldRejectMissingFulfillmentTypeBeforeFulfillmentRules() {
        UUID sellerCompanyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LinkedHashSet<SalesOrderItem> orderItems = items(item());

        assertThatThrownBy(() -> SalesOrder.createB2C(
                        100L, null, PaymentMethod.PIX, null, sellerCompanyId, customerId, orderItems))
                .isInstanceOf(DomainValidationException.class)
                .satisfies(exception -> assertThat(((DomainValidationException) exception).getErrorCode())
                        .isEqualTo(SalesOrderErrorCode.FULFILLMENT_TYPE_REQUIRED));
    }

    @Test
    void shouldCreateB2bOrderWithBuyerCompanyAndNoCustomer() {
        UUID buyerCompanyId = UUID.randomUUID();

        SalesOrder order = SalesOrder.createB2B(
                100L,
                FulfillmentType.PICKUP,
                PaymentMethod.PIX,
                null,
                UUID.randomUUID(),
                buyerCompanyId,
                items(item()));

        assertThat(order.getSaleType()).isEqualTo(SaleType.B2B);
        assertThat(order.getBuyerCompanyId()).isEqualTo(buyerCompanyId);
        assertThat(order.getCustomerId()).isNull();
    }

    @Test
    void shouldCreateB2cOrderWithCustomerAndNoBuyerCompany() {
        UUID customerId = UUID.randomUUID();

        SalesOrder order = SalesOrder.createB2C(
                100L, FulfillmentType.PICKUP, PaymentMethod.PIX, null, UUID.randomUUID(), customerId, items(item()));

        assertThat(order.getSaleType()).isEqualTo(SaleType.B2C);
        assertThat(order.getBuyerCompanyId()).isNull();
        assertThat(order.getCustomerId()).isEqualTo(customerId);
    }

    @Test
    void shouldRejectB2bWithoutBuyerCompany() {
        UUID sellerCompanyId = UUID.randomUUID();
        LinkedHashSet<SalesOrderItem> orderItems = items(item());

        assertThatThrownBy(() -> SalesOrder.createB2B(
                        100L, FulfillmentType.PICKUP, PaymentMethod.PIX, null, sellerCompanyId, null, orderItems))
                .isInstanceOf(DomainValidationException.class)
                .satisfies(exception -> assertThat(((DomainValidationException) exception).getErrorCode())
                        .isEqualTo(SalesOrderErrorCode.BUYER_COMPANY_REQUIRED_FOR_B2B));
    }

    @Test
    void shouldRejectB2cWithoutCustomer() {
        UUID sellerCompanyId = UUID.randomUUID();
        LinkedHashSet<SalesOrderItem> orderItems = items(item());

        assertThatThrownBy(() -> SalesOrder.createB2C(
                        100L, FulfillmentType.PICKUP, PaymentMethod.PIX, null, sellerCompanyId, null, orderItems))
                .isInstanceOf(DomainValidationException.class)
                .satisfies(exception -> assertThat(((DomainValidationException) exception).getErrorCode())
                        .isEqualTo(SalesOrderErrorCode.CUSTOMER_REQUIRED_FOR_B2C));
    }

    @Test
    void shouldRejectItemsWithDifferentCurrencies() {
        UUID sellerCompanyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LinkedHashSet<SalesOrderItem> orderItems = items(item("BRL"), item("USD"));

        assertThatThrownBy(() -> SalesOrder.createB2C(
                        100L, FulfillmentType.PICKUP, PaymentMethod.PIX, null, sellerCompanyId, customerId, orderItems))
                .isInstanceOf(SalesOrderException.class)
                .satisfies(exception -> assertThat(((SalesOrderException) exception).getErrorCode())
                        .isEqualTo(SalesOrderErrorCode.ITEMS_MUST_USE_SINGLE_CURRENCY));
    }

    @Test
    void shouldRejectConfirmWhenOrderIsNotDraft() {
        SalesOrder order = order(item());
        order.confirm();

        assertThatThrownBy(order::confirm)
                .isInstanceOf(SalesOrderException.class)
                .satisfies(exception -> assertThat(((SalesOrderException) exception).getErrorCode())
                        .isEqualTo(SalesOrderErrorCode.MUST_BE_DRAFT_TO_CONFIRM));
    }

    @Test
    void shouldRejectItemAlreadyAssignedToAnotherOrder() {
        SalesOrderItem item = item();
        order(item);

        assertThatThrownBy(() -> order(item))
                .isInstanceOf(SalesOrderItemAssignmentException.class)
                .satisfies(exception -> assertThat(((SalesOrderItemAssignmentException) exception).getErrorCode())
                        .isEqualTo(SalesOrderItemErrorCode.ALREADY_ASSIGNED));
    }

    @Test
    void shouldRejectNonPositiveItemQuantity() {
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        Money unitPrice = Money.of(new BigDecimal("10.00"), "BRL");

        assertThatThrownBy(() -> SalesOrderItem.create(productId, warehouseId, "SKU-001", "Product", 0, unitPrice))
                .isInstanceOf(DomainValidationException.class)
                .satisfies(exception -> assertThat(((DomainValidationException) exception).getErrorCode())
                        .isEqualTo(SalesOrderItemErrorCode.QUANTITY_MUST_BE_POSITIVE));
    }

    @Test
    void shouldRejectBlankItemSku() {
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        Money unitPrice = Money.of(new BigDecimal("10.00"), "BRL");

        assertThatThrownBy(() -> SalesOrderItem.create(productId, warehouseId, " ", "Product", 1, unitPrice))
                .isInstanceOf(DomainValidationException.class)
                .satisfies(exception -> assertThat(((DomainValidationException) exception).getErrorCode())
                        .isEqualTo(SalesOrderItemErrorCode.PRODUCT_SKU_REQUIRED));
    }

    @Test
    void shouldRejectBlankItemProductName() {
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        Money unitPrice = Money.of(new BigDecimal("10.00"), "BRL");

        assertThatThrownBy(() -> SalesOrderItem.create(productId, warehouseId, "SKU-001", " ", 1, unitPrice))
                .isInstanceOf(DomainValidationException.class)
                .satisfies(exception -> assertThat(((DomainValidationException) exception).getErrorCode())
                        .isEqualTo(SalesOrderItemErrorCode.PRODUCT_NAME_REQUIRED));
    }

    @Test
    void shouldTrimItemSkuAndProductName() {
        SalesOrderItem item = SalesOrderItem.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                " SKU-001 ",
                " Product ",
                1,
                Money.of(new BigDecimal("10.00"), "BRL"));

        assertThat(item.getProductSku()).isEqualTo("SKU-001");
        assertThat(item.getProductName()).isEqualTo("Product");
    }

    private static SalesOrder order(SalesOrderItem... items) {
        return SalesOrder.createB2C(
                100L,
                FulfillmentType.PICKUP,
                PaymentMethod.PIX,
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                new LinkedHashSet<>(Arrays.asList(items)));
    }

    private static LinkedHashSet<SalesOrderItem> items(SalesOrderItem... items) {
        return new LinkedHashSet<>(Arrays.asList(items));
    }

    private static SalesOrderItem item() {
        return item("BRL");
    }

    private static SalesOrderItem item(String currencyCode) {
        return SalesOrderItem.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "SKU-001",
                "Product",
                1,
                Money.of(new BigDecimal("10.00"), currencyCode));
    }
}
