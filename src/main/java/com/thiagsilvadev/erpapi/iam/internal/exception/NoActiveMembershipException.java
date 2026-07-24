package com.thiagsilvadev.erpapi.iam.internal.exception;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.thiagsilvadev.erpapi.common.domain.exception.BusinessRuleException;

public class NoActiveMembershipException extends BusinessRuleException {

    @Serial
    private static final long serialVersionUID = 1L;

    public NoActiveMembershipException() {
        super(HttpStatus.UNAUTHORIZED, "No active company membership found.");
    }
}
