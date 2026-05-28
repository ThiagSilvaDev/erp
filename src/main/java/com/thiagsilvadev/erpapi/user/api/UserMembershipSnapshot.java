package com.thiagsilvadev.erpapi.user.api;

import java.util.UUID;

public record UserMembershipSnapshot(UUID userId, UUID companyId, UserRole role, UserMembershipStatus status) {}
