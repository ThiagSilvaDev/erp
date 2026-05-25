package com.thiagsilvadev.erpapi.sales.internal.exception;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainErrorCode;

public enum SalesOrderItemErrorCode implements DomainErrorCode {
    PRODUCT_ID_REQUIRED,
    WAREHOUSE_ID_REQUIRED,
    PRODUCT_SKU_REQUIRED,
    PRODUCT_NAME_REQUIRED,
    QUANTITY_MUST_BE_POSITIVE,
    UNIT_PRICE_REQUIRED,
    ORDER_REQUIRED,
    ALREADY_ASSIGNED,
    NOT_ASSIGNED_TO_ORDER;

    @Override
    public String code() {
        return "SALES_ORDER_ITEM_" + name();
    }
}
