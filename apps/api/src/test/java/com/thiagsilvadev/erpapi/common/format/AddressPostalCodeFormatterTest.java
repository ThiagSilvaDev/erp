package com.thiagsilvadev.erpapi.common.format;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.domain.model.address.Country;

class AddressPostalCodeFormatterTest {

    @Test
    void shouldFormatBrazilianPostalCode() {
        assertThat(AddressPostalCodeFormatter.formatPostalCode("01310200", Country.BR))
                .isEqualTo("01310-200");
    }

    @Test
    void shouldReturnRawPostalCodeForCountriesWithoutFormatter() {
        assertThat(AddressPostalCodeFormatter.formatPostalCode("90210-1234", Country.US))
                .isEqualTo("90210-1234");
    }
}
