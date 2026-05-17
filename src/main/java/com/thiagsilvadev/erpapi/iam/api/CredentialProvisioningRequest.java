package com.thiagsilvadev.erpapi.iam.api;

import java.util.UUID;

import com.thiagsilvadev.erpapi.user.api.UserRole;

public record CredentialProvisioningRequest(UUID userId, String email, String rawPassword, UserRole role) {}
