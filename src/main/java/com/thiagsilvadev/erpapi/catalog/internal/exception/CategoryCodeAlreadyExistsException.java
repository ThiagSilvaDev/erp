package com.thiagsilvadev.erpapi.catalog.internal.exception;

import java.io.Serial;
import java.util.Map;

import com.thiagsilvadev.erpapi.common.exception.ConflictException;

public class CategoryCodeAlreadyExistsException extends ConflictException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CategoryCodeAlreadyExistsException(String code) {
        super("Category with this code already exists.", Map.of("code", code));
    }
}
