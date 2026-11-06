package com.thiagsilvadev.erpapi.iam.accesscontrol.api;

import java.util.UUID;

public record PlatformRoleActivatedEvent(UUID assignmentId, UUID userId, String role, UUID actorUserId) {}
