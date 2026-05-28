package com.thiagsilvadev.erpapi.iam.internal.dto;

import java.util.UUID;

import com.thiagsilvadev.erpapi.user.api.UserRole;

public record AuthCompanyResponse(UUID companyId, UserRole role) {}
