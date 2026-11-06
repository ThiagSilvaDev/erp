package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.exception;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.thiagsilvadev.erpapi.common.domain.exception.BusinessRuleException;

public class UserActorIdRequiredException extends BusinessRuleException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UserActorIdRequiredException() {
        super(HttpStatus.BAD_REQUEST, "A user actor requires an actor user id.");
    }
}
