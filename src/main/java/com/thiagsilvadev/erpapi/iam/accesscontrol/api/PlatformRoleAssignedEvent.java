package com.thiagsilvadev.erpapi.iam.accesscontrol.api;

import java.util.UUID;

public record PlatformRoleAssignedEvent(UUID assignmentId, UUID userId, String role, UUID actorUserId) {}
