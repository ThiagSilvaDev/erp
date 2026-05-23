package com.thiagsilvadev.erpapi.inventory.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;
import com.thiagsilvadev.erpapi.inventory.internal.exception.InsufficientInventoryAvailableException;
import com.thiagsilvadev.erpapi.inventory.internal.exception.InsufficientReservedInventoryException;
import com.thiagsilvadev.erpapi.inventory.internal.exception.InventoryErrorCode;

class InventoryItemTest {

    private final Warehouse warehouse = Warehouse.create("MAIN", "Main Warehouse");
    private final UUID productId = UUID.randomUUID();

    @Test
    void shouldCreateInventoryItem() {
        InventoryItem item = InventoryItem.create(productId, warehouse, 10);

        assertThat(item.getProductId()).isEqualTo(productId);
        assertThat(item.getWarehouse()).isSameAs(warehouse);
        assertThat(item.getQuantityOnHand()).isEqualTo(10);
        assertThat(item.getReservedQuantity()).isZero();
        assertThat(item.availableQuantity()).isEqualTo(10);
    }

    @Test
    void shouldRejectNegativeInitialQuantity() {
        assertThatThrownBy(() -> InventoryItem.create(productId, warehouse, -1))
                .isInstanceOf(DomainValidationException.class)
                .extracting(ex -> ((DomainValidationException) ex).getErrorCode())
                .isEqualTo(InventoryErrorCode.QUANTITY_ON_HAND_MUST_BE_NON_NEGATIVE);
    }

    @Test
    void shouldIncreaseQuantityOnHand() {
        InventoryItem item = InventoryItem.create(productId, warehouse, 10);

        item.increase(5);

        assertThat(item.getQuantityOnHand()).isEqualTo(15);
        assertThat(item.getReservedQuantity()).isZero();
        assertThat(item.availableQuantity()).isEqualTo(15);
    }

    @Test
    void shouldDecreaseOnlyAvailableQuantity() {
        InventoryItem item = InventoryItem.create(productId, warehouse, 10);
        item.reserve(6);

        item.decrease(4);

        assertThat(item.getQuantityOnHand()).isEqualTo(6);
        assertThat(item.getReservedQuantity()).isEqualTo(6);
        assertThat(item.availableQuantity()).isZero();
    }

    @Test
    void shouldThrowExceptionWhenDecreasingMoreThanAvailableQuantity() {
        InventoryItem item = InventoryItem.create(productId, warehouse, 10);
        item.reserve(7);

        assertThatThrownBy(() -> item.decrease(4))
                .isInstanceOf(InsufficientInventoryAvailableException.class)
                .hasMessage("Insufficient inventory available");
    }

    @Test
    void shouldReserveAvailableQuantity() {
        InventoryItem item = InventoryItem.create(productId, warehouse, 10);

        item.reserve(3);

        assertThat(item.getQuantityOnHand()).isEqualTo(10);
        assertThat(item.getReservedQuantity()).isEqualTo(3);
        assertThat(item.availableQuantity()).isEqualTo(7);
    }

    @Test
    void shouldThrowExceptionWhenReservingMoreThanAvailableQuantity() {
        InventoryItem item = InventoryItem.create(productId, warehouse, 10);
        item.reserve(8);

        assertThatThrownBy(() -> item.reserve(3))
                .isInstanceOf(InsufficientInventoryAvailableException.class)
                .hasMessage("Insufficient inventory available");
    }

    @Test
    void shouldReleaseReservedQuantity() {
        InventoryItem item = InventoryItem.create(productId, warehouse, 10);
        item.reserve(6);

        item.release(4);

        assertThat(item.getQuantityOnHand()).isEqualTo(10);
        assertThat(item.getReservedQuantity()).isEqualTo(2);
        assertThat(item.availableQuantity()).isEqualTo(8);
    }

    @Test
    void shouldThrowExceptionWhenReleasingMoreThanReservedQuantity() {
        InventoryItem item = InventoryItem.create(productId, warehouse, 10);
        item.reserve(3);

        assertThatThrownBy(() -> item.release(4))
                .isInstanceOf(InsufficientReservedInventoryException.class)
                .hasMessage("Insufficient reserved inventory");
    }

    @Test
    void shouldConsumeReservedQuantity() {
        InventoryItem item = InventoryItem.create(productId, warehouse, 10);
        item.reserve(6);

        item.consumeReserved(4);

        assertThat(item.getQuantityOnHand()).isEqualTo(6);
        assertThat(item.getReservedQuantity()).isEqualTo(2);
        assertThat(item.availableQuantity()).isEqualTo(4);
    }

    @Test
    void shouldThrowExceptionWhenConsumingMoreThanReservedQuantity() {
        InventoryItem item = InventoryItem.create(productId, warehouse, 10);
        item.reserve(3);

        assertThatThrownBy(() -> item.consumeReserved(4))
                .isInstanceOf(InsufficientReservedInventoryException.class)
                .hasMessage("Insufficient reserved inventory");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void shouldRejectNonPositiveQuantities(int invalidQuantity) {
        InventoryItem item = InventoryItem.create(productId, warehouse, 10);

        assertSoftly(softly -> {
            softly.assertThatThrownBy(() -> item.increase(invalidQuantity))
                    .isInstanceOf(DomainValidationException.class)
                    .extracting(ex -> ((DomainValidationException) ex).getErrorCode())
                    .isEqualTo(InventoryErrorCode.QUANTITY_MUST_BE_POSITIVE);
            softly.assertThatThrownBy(() -> item.decrease(invalidQuantity))
                    .isInstanceOf(DomainValidationException.class)
                    .extracting(ex -> ((DomainValidationException) ex).getErrorCode())
                    .isEqualTo(InventoryErrorCode.QUANTITY_MUST_BE_POSITIVE);
            softly.assertThatThrownBy(() -> item.reserve(invalidQuantity))
                    .isInstanceOf(DomainValidationException.class)
                    .extracting(ex -> ((DomainValidationException) ex).getErrorCode())
                    .isEqualTo(InventoryErrorCode.QUANTITY_MUST_BE_POSITIVE);
            softly.assertThatThrownBy(() -> item.release(invalidQuantity))
                    .isInstanceOf(DomainValidationException.class)
                    .extracting(ex -> ((DomainValidationException) ex).getErrorCode())
                    .isEqualTo(InventoryErrorCode.QUANTITY_MUST_BE_POSITIVE);
            softly.assertThatThrownBy(() -> item.consumeReserved(invalidQuantity))
                    .isInstanceOf(DomainValidationException.class)
                    .extracting(ex -> ((DomainValidationException) ex).getErrorCode())
                    .isEqualTo(InventoryErrorCode.QUANTITY_MUST_BE_POSITIVE);
        });
    }
}
