package com.thiagsilvadev.erpapi.iam.api;

import java.util.UUID;

public record CredentialProvisioningRequest(UUID userId, String email, String rawPassword, String role) {}
