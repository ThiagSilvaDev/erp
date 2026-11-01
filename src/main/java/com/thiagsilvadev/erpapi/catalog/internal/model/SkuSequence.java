package com.thiagsilvadev.erpapi.catalog.internal.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.thiagsilvadev.erpapi.catalog.internal.exception.ProductErrorCode;
import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;

@Entity
@Table(name = "catalog_sku_sequences")
public class SkuSequence {
    @Id
    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @Column(name = "current_value", nullable = false)
    private long currentValue;

    protected SkuSequence() {}

    public long next() {
        if (currentValue == Long.MAX_VALUE) {
            throw new DomainValidationException(ProductErrorCode.SKU_SEQUENCE_EXHAUSTED);
        }
        return ++currentValue;
    }
}
