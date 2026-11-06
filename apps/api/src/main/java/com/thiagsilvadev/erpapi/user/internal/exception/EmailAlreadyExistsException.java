package com.thiagsilvadev.erpapi.user.internal.exception;

import java.io.Serial;
import java.util.Map;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;

public class EmailAlreadyExistsException extends ConflictException {

    @Serial
    private static final long serialVersionUID = 1L;

    public EmailAlreadyExistsException(String email) {
        super("User with this email already exists.", Map.of("email", email));
    }
}
