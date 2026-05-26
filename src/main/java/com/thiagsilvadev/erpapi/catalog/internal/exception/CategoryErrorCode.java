package com.thiagsilvadev.erpapi.catalog.internal.exception;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainErrorCode;

public enum CategoryErrorCode implements DomainErrorCode {
    CODE_REQUIRED,
    NAME_REQUIRED;

    @Override
    public String code() {
        return "CATALOG_CATEGORY_" + name();
    }
}
