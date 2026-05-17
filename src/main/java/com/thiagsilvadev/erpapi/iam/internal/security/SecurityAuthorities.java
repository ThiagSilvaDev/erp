package com.thiagsilvadev.erpapi.iam.internal.security;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class SecurityAuthorities {

    private SecurityAuthorities() {}

    public static Collection<GrantedAuthority> fromRole(String role, RolePermissionResolver rolePermissionResolver) {
        if (role == null || role.isBlank()) {
            return List.of();
        }

        return Stream.concat(
                        Stream.of("ROLE_" + role.trim().toUpperCase(Locale.ROOT)),
                        rolePermissionResolver.resolve(role).stream())
                .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                .toList();
    }
}
