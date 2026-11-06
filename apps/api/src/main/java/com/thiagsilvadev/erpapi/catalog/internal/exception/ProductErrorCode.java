package com.thiagsilvadev.erpapi.catalog.internal.exception;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainErrorCode;

public enum ProductErrorCode implements DomainErrorCode {
    SKU_REQUIRED,
    SKU_TOO_LONG,
    SKU_SEQUENCE_EXHAUSTED,
    NAME_REQUIRED,
    BASE_PRICE_REQUIRED,
    CATEGORY_REQUIRED;

    @Override
    public String code() {
        return "CATALOG_PRODUCT_" + name();
    }
}
