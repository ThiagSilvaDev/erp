package com.thiagsilvadev.erpapi.common.format;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ContactFormatterTest {

    @Test
    void shouldFormatBrazilianMobilePhone() {
        assertThat(ContactFormatter.formatBrazilianPhone("11912345678")).isEqualTo("(11) 91234-5678");
    }

    @Test
    void shouldFormatBrazilianLandlinePhone() {
        assertThat(ContactFormatter.formatBrazilianPhone("1112345678")).isEqualTo("(11) 1234-5678");
    }

    @Test
    void shouldReturnNullWhenPhoneIsMissing() {
        assertThat(ContactFormatter.formatBrazilianPhone(null)).isNull();
    }
}
