package com.thiagsilvadev.erpapi.inventory.internal.exception;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainErrorCode;

public enum InventoryErrorCode implements DomainErrorCode {
    QUANTITY_ON_HAND_MUST_BE_NON_NEGATIVE,
    QUANTITY_MUST_BE_POSITIVE,
    INSUFFICIENT_AVAILABLE,
    INSUFFICIENT_RESERVED
}
