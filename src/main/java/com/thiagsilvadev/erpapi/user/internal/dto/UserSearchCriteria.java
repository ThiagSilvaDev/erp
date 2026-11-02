package com.thiagsilvadev.erpapi.user.internal.dto;

import jakarta.validation.constraints.Size;

import com.thiagsilvadev.erpapi.user.api.UserStatus;

public record UserSearchCriteria(@Size(max = 255) String email, @Size(max = 120) String name, UserStatus status) {}
