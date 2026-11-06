package com.thiagsilvadev.erpapi.user.api;

import java.util.UUID;

/**
 * Published through the user module event publisher whenever a user is created.
 *
 * <p>This record is part of the module's public contract and is deliberately published without
 * in-process subscribers: it reserves the integration point for future consumers such as
 * notifications or outbound webhooks. Do not remove it as unused code; add a subscriber instead.
 */
public record UserCreatedEvent(UUID userId, String email) {}
