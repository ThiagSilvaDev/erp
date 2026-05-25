package com.thiagsilvadev.erpapi.sales.internal.exception;

import java.io.Serial;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainErrorCode;
import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;

public class SalesOrderItemAssignmentException extends DomainValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SalesOrderItemAssignmentException(DomainErrorCode errorCode) {
        super(errorCode);
    }
}
