package com.thiagsilvadev.erpapi.common.domain.model.address;

import java.util.Map;

final class AddressCountryRulesRegistry {

    private static final AddressCountryRules DEFAULT_RULES = new DefaultAddressCountryRules();
    private static final Map<Country, AddressCountryRules> RULES = Map.of(Country.BR, new BrazilAddressCountryRules());

    private AddressCountryRulesRegistry() {}

    static AddressCountryRules get(Country country) {
        return RULES.getOrDefault(country, DEFAULT_RULES);
    }
}
