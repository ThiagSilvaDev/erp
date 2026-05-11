package com.thiagsilvadev.erpapi.common.exception;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import org.springframework.http.HttpStatus;

public abstract class BusinessRuleException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final HttpStatus status;
    private final Map<String, Serializable> properties;

    protected BusinessRuleException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.properties = Map.of();
    }

    protected BusinessRuleException(HttpStatus status, String detail, Map<String, Serializable> properties) {
        super(detail);
        this.status = status;
        this.properties = Map.copyOf(properties);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, Serializable> getProperties() {
        return properties;
    }
}
