package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.permission.ModulePermissionProvider;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.IamPermissions;

@Component
public class PermissionCatalog {

    public static final String PLATFORM_COMPANY_READ = "platform:company:read";
    public static final String PLATFORM_COMPANY_WRITE = "platform:company:write";

    private final Set<String> tenantPermissions;
    private final Set<String> platformPermissions;

    public PermissionCatalog(List<ModulePermissionProvider> permissionProviders) {
        Set<String> tenant = new HashSet<>(IamPermissions.ALL);
        Set<String> platform = new HashSet<>(Set.of(PLATFORM_COMPANY_READ, PLATFORM_COMPANY_WRITE));
        for (ModulePermissionProvider provider : permissionProviders) {
            tenant.addAll(provider.tenantGrantablePermissions());
            platform.addAll(provider.allPermissions());
        }
        tenantPermissions = Set.copyOf(tenant);
        platformPermissions = Set.copyOf(platform);
    }

    public Set<String> tenantPermissions() {
        return tenantPermissions;
    }

    public Set<String> platformPermissions() {
        return platformPermissions;
    }

    public void validateTenantPermissions(Set<String> permissions) {
        Set<String> unknown = new HashSet<>(permissions);
        unknown.removeAll(tenantPermissions);
        if (!unknown.isEmpty()) {
            throw new IllegalArgumentException("Unknown or non-tenant permissions: " + unknown);
        }
    }

    public void validateGrantable(Set<String> permissions) {
        validateTenantPermissions(permissions);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities =
                authentication == null ? null : authentication.getAuthorities();
        Set<String> callerPermissions = authorities == null
                ? Set.of()
                : authorities.stream()
                        .filter(Objects::nonNull)
                        .map(GrantedAuthority::getAuthority)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
        if (!callerPermissions.containsAll(permissions)) {
            throw new AccessDeniedException("A role cannot grant permissions the caller does not possess.");
        }
    }
}
