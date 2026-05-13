package com.thiagsilvadev.erpapi.catalog.internal.exception;

import java.io.Serial;
import java.util.Map;

import com.thiagsilvadev.erpapi.common.exception.ConflictException;

public class SkuAlreadyExistsException extends ConflictException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SkuAlreadyExistsException(String sku) {
        super("Product with this SKU already exists.", Map.of("sku", sku));
    }
}
