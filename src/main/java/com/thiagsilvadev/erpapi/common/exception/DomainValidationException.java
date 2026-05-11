package com.thiagsilvadev.erpapi.common.exception;

import java.io.Serial;

public class DomainValidationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DomainValidationException(String message) {
        super(message);
    }

    public DomainValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
