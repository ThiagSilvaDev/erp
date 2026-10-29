package com.thiagsilvadev.erpapi.iam.internal.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtClaimsValidatorTest {
    private final Instant now = Instant.parse("2026-05-16T00:00:00Z");
    private final JwtClaimsValidator validator = new JwtClaimsValidator(Clock.fixed(now, ZoneOffset.UTC));

    private Map<String, Object> claims() {
        var c = new HashMap<String, Object>();
        c.put("sub", UUID.randomUUID().toString());
        c.put("jti", "unique");
        c.put("token_use", "global");
        c.put("iat", now);
        c.put("nbf", now);
        c.put("exp", now.plusSeconds(3600));
        return c;
    }

    private boolean invalid(Map<String, Object> c) {
        return validator
                .validate(Jwt.withTokenValue("token")
                        .header("alg", "HS256")
                        .claims(values -> values.putAll(c))
                        .build())
                .hasErrors();
    }

    @Test
    void acceptsGlobal() {
        assertThat(invalid(claims())).isFalse();
    }

    @Test
    void rejectsMissingRequiredClaims() {
        for (String name : new String[] {"sub", "jti", "token_use", "iat", "nbf", "exp"}) {
            var c = claims();
            c.remove(name);
            assertThat(invalid(c)).as(name).isTrue();
        }
    }

    @Test
    void rejectsConflictingContext() {
        var c = claims();
        c.put("tenant_id", UUID.randomUUID().toString());
        assertThat(invalid(c)).isTrue();
    }

    @Test
    void validatesScopedVersionAndCompany() {
        var c = claims();
        c.put("token_use", "scoped");
        c.put("tenant_id", UUID.randomUUID().toString());
        c.put("membership_id", UUID.randomUUID().toString());
        c.put("authz_version", 0L);
        assertThat(invalid(c)).isFalse();
        for (Object version : new Object[] {-1L, 0.5, "1", 1e30}) {
            c.put("authz_version", version);
            assertThat(invalid(c)).isTrue();
        }
        c.put("authz_version", 0L);
        c.put("company_id", UUID.randomUUID().toString());
        assertThat(invalid(c)).isTrue();
    }

    @Test
    void validatesPlatform() {
        var c = claims();
        c.put("token_use", "platform");
        c.put("platform_assignment_id", UUID.randomUUID().toString());
        c.put("authz_version", 1L);
        assertThat(invalid(c)).isFalse();
    }

    @Test
    void rejectsFutureIssuance() {
        var c = claims();
        c.put("iat", now.plusSeconds(61));
        assertThat(invalid(c)).isTrue();
    }
}
