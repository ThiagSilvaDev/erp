package com.thiagsilvadev.erpapi.common.model.address;

public sealed interface AddressCountryRules permits BrazilAddressCountryRules, DefaultAddressCountryRules {
    Country country();

    String normalizePostalCode(String postalCode);

    String normalizeRegion(String region);
}
