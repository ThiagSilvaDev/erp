package com.thiagsilvadev.erpapi.iam.internal.dto;

import java.util.UUID;

public record AuthUserResponse(UUID id, String email, String role) {}
