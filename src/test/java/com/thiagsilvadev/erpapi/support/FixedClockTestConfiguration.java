package com.thiagsilvadev.erpapi.support;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class FixedClockTestConfiguration {

    public static final Instant FIXED_INSTANT = Instant.parse("2026-05-11T00:00:00Z");

    @Bean
    Clock clock() {
        return Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
    }
}
