package com.thiagsilvadev.erpapi.user.internal.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.thiagsilvadev.erpapi.user.api.UserRole;

public record CreateUserRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        String phone,
        LocalDate birthDate,
        @NotNull UserRole role,
        @NotNull UUID companyId,
        @NotBlank @Size(min = 8, max = 72) String password) {}
