package com.thiagsilvadev.erpapi.user.internal.dto;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        String phone,
        LocalDate birthDate,
        @NotNull @NotEmpty Set<@NotNull UUID> roleIds,
        @NotBlank @Size(min = 8, max = 72) String password) {}
