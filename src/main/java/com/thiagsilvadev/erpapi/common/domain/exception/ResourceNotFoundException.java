package com.thiagsilvadev.erpapi.common.domain.exception;

import java.io.Serial;
import java.util.Map;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessRuleException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(
                HttpStatus.NOT_FOUND,
                "Resource not found.",
                Map.of("resourceName", resourceName, "identifier", String.valueOf(identifier)));
    }

    public ResourceNotFoundException(Class<?> resourceClass, Object identifier) {
        this(resourceClass.getSimpleName(), identifier);
    }
}
