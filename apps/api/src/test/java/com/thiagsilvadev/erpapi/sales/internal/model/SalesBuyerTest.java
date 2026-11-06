package com.thiagsilvadev.erpapi.sales.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;
import com.thiagsilvadev.erpapi.sales.api.SaleType;
import com.thiagsilvadev.erpapi.sales.internal.exception.SalesOrderErrorCode;
import com.thiagsilvadev.erpapi.sales.internal.exception.SalesOrderException;

class SalesBuyerTest {

    @Test
    void shouldCreateB2bBuyer() {
        UUID buyerCompanyId = UUID.randomUUID();

        SalesBuyer buyer = SalesBuyer.company(buyerCompanyId);

        assertThat(buyer.getSaleType()).isEqualTo(SaleType.B2B);
        assertThat(buyer.getBuyerCompanyId()).isEqualTo(buyerCompanyId);
        assertThat(buyer.getCustomerId()).isNull();
    }

    @Test
    void shouldCreateB2cBuyer() {
        UUID customerId = UUID.randomUUID();

        SalesBuyer buyer = SalesBuyer.customer(customerId);

        assertThat(buyer.getSaleType()).isEqualTo(SaleType.B2C);
        assertThat(buyer.getBuyerCompanyId()).isNull();
        assertThat(buyer.getCustomerId()).isEqualTo(customerId);
    }

    @Test
    void shouldRejectB2bWithoutBuyerCompany() {
        DomainValidationException exception =
                catchThrowableOfType(DomainValidationException.class, () -> SalesBuyer.of(SaleType.B2B, null, null));

        assertThat(exception.getErrorCode()).isEqualTo(SalesOrderErrorCode.BUYER_COMPANY_REQUIRED_FOR_B2B);
    }

    @Test
    void shouldRejectB2bWithCustomer() {
        UUID buyerCompanyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        SalesOrderException exception = catchThrowableOfType(
                SalesOrderException.class, () -> SalesBuyer.of(SaleType.B2B, buyerCompanyId, customerId));

        assertThat(exception.getErrorCode()).isEqualTo(SalesOrderErrorCode.CUSTOMER_NOT_ALLOWED_FOR_B2B);
    }

    @Test
    void shouldRejectB2cWithoutCustomer() {
        DomainValidationException exception =
                catchThrowableOfType(DomainValidationException.class, () -> SalesBuyer.of(SaleType.B2C, null, null));

        assertThat(exception.getErrorCode()).isEqualTo(SalesOrderErrorCode.CUSTOMER_REQUIRED_FOR_B2C);
    }

    @Test
    void shouldRejectB2cWithBuyerCompany() {
        UUID buyerCompanyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        SalesOrderException exception = catchThrowableOfType(
                SalesOrderException.class, () -> SalesBuyer.of(SaleType.B2C, buyerCompanyId, customerId));

        assertThat(exception.getErrorCode()).isEqualTo(SalesOrderErrorCode.BUYER_COMPANY_NOT_ALLOWED_FOR_B2C);
    }
}
