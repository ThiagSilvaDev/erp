package com.thiagsilvadev.erpapi.common.domain.model.address;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;
import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;

final class BrazilAddressCountryRules implements AddressCountryRules {

    @Override
    public Country country() {
        return Country.BR;
    }

    @Override
    public String normalizePostalCode(String rawPostalCode) {
        DomainAssert.notNull(rawPostalCode, "CEP code is required");

        String normalizedPostalCode = rawPostalCode.replaceAll("\\D", "");

        DomainAssert.exactLength(normalizedPostalCode, 8, "CEP code must contain 8 digits");
        return normalizedPostalCode;
    }

    @Override
    public String normalizeRegion(String rawRegion) {
        String value = Address.processRequiredText(rawRegion, "UF", 2).toUpperCase();

        try {
            UF.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new DomainValidationException("Invalid UF", e);
        }

        return value;
    }
}
