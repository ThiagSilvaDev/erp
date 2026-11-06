package com.thiagsilvadev.erpapi.common.format;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CnpjFormatterTest {

    @Test
    void shouldFormatCnpj() {
        assertThat(CnpjFormatter.format("11222333000181")).isEqualTo("11.222.333/0001-81");
    }
}
