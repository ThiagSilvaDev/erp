package com.thiagsilvadev.erpapi.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.data.auditing.DateTimeProvider;

class CommonConfigurationTest {

    @Test
    void deveCriarDateTimeProviderComClockConfigurado() {
        Instant instant = Instant.parse("2026-05-11T00:00:00Z");
        Clock clock = Clock.fixed(instant, ZoneOffset.UTC);
        CommonConfiguration configuration = new CommonConfiguration();

        DateTimeProvider provider = configuration.auditingDateTimeProvider(clock);

        assertThat(provider.getNow()).isEqualTo(Optional.of(instant));
    }
}
