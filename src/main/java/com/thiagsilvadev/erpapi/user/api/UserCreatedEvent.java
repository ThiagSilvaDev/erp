package com.thiagsilvadev.erpapi.user.api;

import java.util.UUID;

public record UserCreatedEvent(UUID userId, String email, UUID companyId, UserRole role) {

    public static UserCreatedEvent created(UUID userId, String email, UUID companyId, UserRole role) {
        return new UserCreatedEvent(userId, email, companyId, role);
    }
}
