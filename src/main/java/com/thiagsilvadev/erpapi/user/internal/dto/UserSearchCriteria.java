package com.thiagsilvadev.erpapi.user.internal.dto;

import java.util.UUID;

import com.thiagsilvadev.erpapi.user.api.UserRole;
import com.thiagsilvadev.erpapi.user.api.UserStatus;

public record UserSearchCriteria(String email, String name, UserRole role, UserStatus status, UUID companyId) {}
