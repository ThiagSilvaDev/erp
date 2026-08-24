package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ReplaceMembershipRolesRequest(@NotEmpty Set<@NotNull UUID> roleIds) {}
