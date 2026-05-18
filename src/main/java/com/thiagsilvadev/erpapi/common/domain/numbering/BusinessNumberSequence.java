package com.thiagsilvadev.erpapi.common.domain.numbering;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "business_number_sequences")
public class BusinessNumberSequence {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 64)
    private BusinessNumberType type;

    @Column(nullable = false)
    private Long currentValue;

    protected BusinessNumberSequence() {}

    private BusinessNumberSequence(BusinessNumberType type, Long currentValue) {
        this.type = type;
        this.currentValue = currentValue;
    }

    public static BusinessNumberSequence create(BusinessNumberType type) {
        return new BusinessNumberSequence(type, 0L);
    }

    public Long next() {
        currentValue++;
        return currentValue;
    }

    public BusinessNumberType getType() {
        return type;
    }

    public Long getCurrentValue() {
        return currentValue;
    }
}
