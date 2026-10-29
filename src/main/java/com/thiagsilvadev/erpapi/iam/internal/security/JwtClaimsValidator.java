package com.thiagsilvadev.erpapi.iam.internal.security;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.UUID;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class JwtClaimsValidator implements OAuth2TokenValidator<Jwt> {
    private final Clock clock;

    public JwtClaimsValidator(Clock clock) {
        this.clock = clock;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        try {
            uuid(jwt, "sub");
            text(jwt, "jti");
            if (jwt.getIssuedAt() == null
                    || jwt.getNotBefore() == null
                    || jwt.getExpiresAt() == null
                    || !jwt.getExpiresAt().isAfter(jwt.getIssuedAt())
                    || jwt.getNotBefore().isAfter(jwt.getExpiresAt())
                    || jwt.getIssuedAt().isAfter(clock.instant().plusSeconds(60))) throw new IllegalArgumentException();
            String use = text(jwt, "token_use");
            switch (use) {
                case "scoped" -> {
                    uuid(jwt, "tenant_id");
                    uuid(jwt, "membership_id");
                    version(jwt);
                    absent(jwt, "platform_assignment_id");
                    if (jwt.hasClaim("company_id") && !uuid(jwt, "company_id").equals(uuid(jwt, "tenant_id")))
                        throw new IllegalArgumentException();
                }
                case "platform" -> {
                    uuid(jwt, "platform_assignment_id");
                    version(jwt);
                    absent(jwt, "tenant_id", "company_id", "membership_id");
                }
                case "global" ->
                    absent(jwt, "tenant_id", "company_id", "membership_id", "platform_assignment_id", "authz_version");
                default -> throw new IllegalArgumentException();
            }
            return OAuth2TokenValidatorResult.success();
        } catch (IllegalArgumentException | ClassCastException | ArithmeticException exception) {
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid JWT claims", null));
        }
    }

    private static String text(Jwt jwt, String name) {
        Object value = jwt.getClaim(name);
        if (!(value instanceof String text) || text.isBlank()) throw new IllegalArgumentException();
        return text;
    }

    private static UUID uuid(Jwt jwt, String name) {
        String value = text(jwt, name);
        UUID uuid = UUID.fromString(value);
        if (!uuid.toString().equalsIgnoreCase(value)) throw new IllegalArgumentException();
        return uuid;
    }

    private static void absent(Jwt jwt, String... names) {
        for (String name : names) if (jwt.hasClaim(name)) throw new IllegalArgumentException();
    }

    private static void version(Jwt jwt) {
        Object value = jwt.getClaim("authz_version");
        if (!(value instanceof Number number) || new BigDecimal(number.toString()).longValueExact() < 0)
            throw new IllegalArgumentException();
    }
}
