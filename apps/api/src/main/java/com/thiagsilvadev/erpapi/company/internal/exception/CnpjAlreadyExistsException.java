package com.thiagsilvadev.erpapi.company.internal.exception;

import java.io.Serial;
import java.util.Map;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;

public class CnpjAlreadyExistsException extends ConflictException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CnpjAlreadyExistsException(String cnpj) {
        super("Company com esse CNPJ ja existe.", Map.of("cnpj", cnpj));
    }
}
