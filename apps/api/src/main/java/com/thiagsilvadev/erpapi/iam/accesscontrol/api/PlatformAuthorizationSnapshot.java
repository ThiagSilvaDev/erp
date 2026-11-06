package com.thiagsilvadev.erpapi.iam.accesscontrol.api;

import java.util.Set;
import java.util.UUID;

public record PlatformAuthorizationSnapshot(
        UUID assignmentId, UUID userId, long authorizationVersion, String role, Set<String> permissions) {}
