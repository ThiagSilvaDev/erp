package com.thiagsilvadev.erpapi.iam.internal.exception;

import java.util.Map;

import com.thiagsilvadev.erpapi.common.exception.ConflictException;

public class CredentialAlreadyExistsException extends ConflictException {

    public CredentialAlreadyExistsException(String field, Object value) {
        super("IAM credential already exists.", Map.of(field, String.valueOf(value)));
    }
}
