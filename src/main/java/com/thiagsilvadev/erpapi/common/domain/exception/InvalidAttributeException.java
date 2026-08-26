package com.thiagsilvadev.erpapi.common.domain.exception;

import java.io.Serial;

public class InvalidAttributeException extends DomainValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String attribute;

    @SuppressWarnings("deprecation")
    private InvalidAttributeException(String attribute, String reason) {
        super(attribute + " " + reason);
        this.attribute = attribute;
    }

    public static InvalidAttributeException required(String attribute) {
        return new InvalidAttributeException(attribute, "is required");
    }

    public static InvalidAttributeException cannotBeNull(String attribute) {
        return new InvalidAttributeException(attribute, "cannot be null");
    }

    public static InvalidAttributeException cannotBeBlank(String attribute) {
        return new InvalidAttributeException(attribute, "cannot be blank");
    }

    public String getAttribute() {
        return attribute;
    }
}
