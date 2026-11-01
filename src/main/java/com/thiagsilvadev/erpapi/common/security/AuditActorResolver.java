package com.thiagsilvadev.erpapi.common.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Resolves the acting user id from the current security context. Used by audit-facing publishers
 * so that the actor is captured exactly once, at event build time, with a single implementation.
 *
 * <p>An empty result means the actor could not be determined; callers should propagate it as an
 * unknown actor instead of silently claiming a system actor.</p>
 */
@Component
public class AuditActorResolver {

    public Optional<UUID> userId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken token)) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(token.getToken().getSubject()));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    /** Actor id for audit payloads, where an unknown actor is reported as {@code null}. */
    public UUID userIdOrNull() {
        return userId().orElse(null);
    }
}
