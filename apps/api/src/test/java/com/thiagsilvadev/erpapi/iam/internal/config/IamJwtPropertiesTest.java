package com.thiagsilvadev.erpapi.iam.internal.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class IamJwtPropertiesTest {
    private IamJwtProperties valid() {
        var p = new IamJwtProperties();
        p.setSecret("a".repeat(32));
        return p;
    }

    @Test
    void acceptsValidConfiguration() {
        assertThatCode(valid()::afterPropertiesSet).doesNotThrowAnyException();
    }

    @Test
    void rejectsInvalidDurations() {
        for (Duration d : new Duration[] {Duration.ZERO, Duration.ofSeconds(-1), Duration.ofMillis(1500)}) {
            var p = valid();
            p.setExpiration(d);
            assertThatThrownBy(p::afterPropertiesSet).isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    void rejectsBlankIssuerAndSecret() {
        var p = valid();
        p.setIssuer(" ");
        assertThatThrownBy(p::afterPropertiesSet).isInstanceOf(IllegalStateException.class);
        var q = valid();
        q.setSecret(" ".repeat(32));
        assertThatThrownBy(q::afterPropertiesSet).isInstanceOf(IllegalStateException.class);
    }
}
