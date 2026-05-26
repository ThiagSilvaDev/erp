package com.thiagsilvadev.erpapi.sales.internal.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;
import com.thiagsilvadev.erpapi.sales.api.SaleType;
import com.thiagsilvadev.erpapi.sales.internal.exception.SalesOrderErrorCode;
import com.thiagsilvadev.erpapi.sales.internal.exception.SalesOrderException;

@Embeddable
public class SalesBuyer {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private SaleType saleType;

    @Column(updatable = false)
    private UUID buyerCompanyId;

    @Column(updatable = false)
    private UUID customerId;

    protected SalesBuyer() {}

    private SalesBuyer(SaleType saleType, UUID buyerCompanyId, UUID customerId) {
        this.saleType = saleType;
        this.buyerCompanyId = buyerCompanyId;
        this.customerId = customerId;
    }

    public static SalesBuyer of(SaleType saleType, UUID buyerCompanyId, UUID customerId) {
        SaleType requiredSaleType = DomainAssert.requireNonNull(saleType, SalesOrderErrorCode.SALE_TYPE_REQUIRED);

        if (requiredSaleType == SaleType.B2B) {
            return company(buyerCompanyId, customerId);
        }

        return customer(buyerCompanyId, customerId);
    }

    public static SalesBuyer company(UUID buyerCompanyId) {
        return company(buyerCompanyId, null);
    }

    public static SalesBuyer customer(UUID customerId) {
        return customer(null, customerId);
    }

    private static SalesBuyer company(UUID buyerCompanyId, UUID customerId) {
        DomainAssert.requireNonNull(buyerCompanyId, SalesOrderErrorCode.BUYER_COMPANY_REQUIRED_FOR_B2B);
        if (customerId != null) {
            throw new SalesOrderException(SalesOrderErrorCode.CUSTOMER_NOT_ALLOWED_FOR_B2B);
        }
        return new SalesBuyer(SaleType.B2B, buyerCompanyId, null);
    }

    private static SalesBuyer customer(UUID buyerCompanyId, UUID customerId) {
        DomainAssert.requireNonNull(customerId, SalesOrderErrorCode.CUSTOMER_REQUIRED_FOR_B2C);
        if (buyerCompanyId != null) {
            throw new SalesOrderException(SalesOrderErrorCode.BUYER_COMPANY_NOT_ALLOWED_FOR_B2C);
        }
        return new SalesBuyer(SaleType.B2C, null, customerId);
    }

    public SaleType getSaleType() {
        return saleType;
    }

    public UUID getBuyerCompanyId() {
        return buyerCompanyId;
    }

    public UUID getCustomerId() {
        return customerId;
    }
}
