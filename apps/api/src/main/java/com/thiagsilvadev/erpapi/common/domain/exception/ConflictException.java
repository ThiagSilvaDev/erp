package com.thiagsilvadev.erpapi.common.domain.exception;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

public class ConflictException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<String, Serializable> properties;

    public ConflictException(String message) {
        super(message);
        this.properties = Map.of();
    }

    public ConflictException(String message, Map<String, Serializable> properties) {
        super(message);
        this.properties = Map.copyOf(properties);
    }

    public Map<String, Serializable> getProperties() {
        return properties;
    }
}
