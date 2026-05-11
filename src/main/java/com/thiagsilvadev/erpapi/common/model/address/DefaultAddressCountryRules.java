package com.thiagsilvadev.erpapi.common.model.address;

final class DefaultAddressCountryRules implements AddressCountryRules {

    @Override
    public Country country() {
        return null;
    }

    @Override
    public String normalizePostalCode(String postalCode) {
        return Address.processRequiredText(postalCode, "Postal code", 30);
    }

    @Override
    public String normalizeRegion(String region) {
        return Address.processRequiredText(region, "Region/state", 50);
    }
}
