package com.thiagsilvadev.erpapi.common.domain.exception;

import java.io.Serial;

import org.jspecify.annotations.Nullable;

public class DomainValidationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final @Nullable DomainErrorCode errorCode;

    @Deprecated(since = "0.0.1")
    public DomainValidationException(String message) {
        super(message);
        this.errorCode = null;
    }

    @Deprecated(since = "0.0.1")
    public DomainValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    public DomainValidationException(DomainErrorCode errorCode) {
        super(errorCode.code());
        this.errorCode = errorCode;
    }

    public @Nullable DomainErrorCode getErrorCode() {
        return errorCode;
    }
}
