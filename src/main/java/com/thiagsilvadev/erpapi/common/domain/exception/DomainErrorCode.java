package com.thiagsilvadev.erpapi.common.domain.exception;

import java.io.Serializable;

public interface DomainErrorCode extends Serializable {

    String name();

    default String code() {
        return name();
    }
}
