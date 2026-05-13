package com.thiagsilvadev.erpapi.user.api;

import java.time.LocalDate;
import java.util.UUID;

public record UserSnapshot(
        UUID id,
        String displayName,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate birthDate,
        UserRole role,
        UserStatus status,
        UUID companyId) {}
