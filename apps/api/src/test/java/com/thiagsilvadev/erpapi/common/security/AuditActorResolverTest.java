package com.thiagsilvadev.erpapi.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class AuditActorResolverTest {

    private final AuditActorResolver resolver = new AuditActorResolver();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldResolveUserIdFromJwtSubject() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(userId.toString())
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        assertThat(resolver.userId()).contains(userId);
    }

    @Test
    void shouldReturnEmptyForNonJwtAuthentication() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("user", null, "ROLE_ADMIN"));

        assertThat(resolver.userId()).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenSubjectIsNotUuid() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("not-a-uuid")
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        assertThat(resolver.userId()).isEmpty();
    }
}
