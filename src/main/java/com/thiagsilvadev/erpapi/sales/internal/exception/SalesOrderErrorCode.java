package com.thiagsilvadev.erpapi.sales.internal.exception;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainErrorCode;

public enum SalesOrderErrorCode implements DomainErrorCode {
    NUMBER_REQUIRED,
    SALE_TYPE_REQUIRED,
    FULFILLMENT_TYPE_REQUIRED,
    PAYMENT_METHOD_REQUIRED,
    SELLER_COMPANY_REQUIRED,
    BUYER_COMPANY_REQUIRED_FOR_B2B,
    CUSTOMER_NOT_ALLOWED_FOR_B2B,
    CUSTOMER_REQUIRED_FOR_B2C,
    BUYER_COMPANY_NOT_ALLOWED_FOR_B2C,
    ITEMS_REQUIRED,
    ITEM_REQUIRED,
    ITEMS_MUST_USE_SINGLE_CURRENCY,
    MUST_BE_DRAFT_TO_CONFIRM,
    MUST_BE_DRAFT_TO_REMOVE_ITEMS;

    @Override
    public String code() {
        return "SALES_ORDER_" + name();
    }
}
