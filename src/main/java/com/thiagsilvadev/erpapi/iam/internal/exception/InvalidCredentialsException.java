package com.thiagsilvadev.erpapi.iam.internal.exception;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.thiagsilvadev.erpapi.common.domain.exception.BusinessRuleException;

public class InvalidCredentialsException extends BusinessRuleException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
    }
}
