package com.thiagsilvadev.erpapi.inventory.internal.exception;

import org.springframework.http.HttpStatus;

import com.thiagsilvadev.erpapi.common.domain.exception.BusinessRuleException;

public class InsufficientReservedInventoryException extends BusinessRuleException {

    public InsufficientReservedInventoryException() {
        super(HttpStatus.UNPROCESSABLE_CONTENT, "Insufficient reserved inventory");
    }
}
