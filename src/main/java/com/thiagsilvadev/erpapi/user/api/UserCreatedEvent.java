package com.thiagsilvadev.erpapi.user.api;

import java.util.UUID;

public record UserCreatedEvent(UUID userId, String email, UUID companyId) {}
