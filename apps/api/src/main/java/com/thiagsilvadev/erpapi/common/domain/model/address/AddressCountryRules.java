package com.thiagsilvadev.erpapi.common.domain.model.address;

public sealed interface AddressCountryRules permits BrazilAddressCountryRules, DefaultAddressCountryRules {
    Country country();

    String normalizePostalCode(String postalCode);

    String normalizeRegion(String region);
}
