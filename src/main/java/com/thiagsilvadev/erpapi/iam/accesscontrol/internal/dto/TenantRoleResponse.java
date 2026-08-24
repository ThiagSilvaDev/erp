package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto;

import java.util.Set;
import java.util.UUID;

import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRoleType;

public record TenantRoleResponse(
        UUID id,
        String code,
        String name,
        String description,
        TenantRoleType type,
        AuthorizationStatus status,
        Set<String> permissions) {}
