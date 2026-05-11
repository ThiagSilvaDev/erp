package com.thiagsilvadev.erpapi.common.format;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CpfFormatterTest {

    @Test
    void shouldFormatCpf() {
        assertThat(CpfFormatter.format("52998224725")).isEqualTo("529.982.247-25");
    }
}
