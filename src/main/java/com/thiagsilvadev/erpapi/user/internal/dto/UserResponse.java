package com.thiagsilvadev.erpapi.user.internal.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.thiagsilvadev.erpapi.user.api.UserRole;
import com.thiagsilvadev.erpapi.user.api.UserStatus;

public record UserResponse(
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
