package com.thiagsilvadev.erpapi.inventory.internal.exception;

import org.springframework.http.HttpStatus;

import com.thiagsilvadev.erpapi.common.domain.exception.BusinessRuleException;

public class InsufficientInventoryAvailableException extends BusinessRuleException {

    public InsufficientInventoryAvailableException() {
        super(HttpStatus.UNPROCESSABLE_CONTENT, "Insufficient inventory available");
    }
}
