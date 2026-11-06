package com.thiagsilvadev.erpapi.common.format;

import com.thiagsilvadev.erpapi.common.domain.model.address.Country;

public final class AddressPostalCodeFormatter {

    private AddressPostalCodeFormatter() {}

    public static String formatPostalCode(String postalCode, Country country) {
        if (country == Country.BR && postalCode != null) {
            return postalCode.replaceFirst("(\\d{5})(\\d{3})", "$1-$2");
        }
        return postalCode;
    }
}
