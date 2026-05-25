package com.thiagsilvadev.erpapi.sales.internal.exception;

import java.io.Serial;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainErrorCode;
import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;

public class SalesOrderException extends DomainValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SalesOrderException(DomainErrorCode errorCode) {
        super(errorCode);
    }
}
