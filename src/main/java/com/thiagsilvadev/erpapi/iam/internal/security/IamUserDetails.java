package com.thiagsilvadev.erpapi.iam.internal.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@NullMarked
public class IamUserDetails implements UserDetails {

    private final UUID userId;
    private final String email;
    private final String passwordHash;
    private final boolean active;
    private final List<GrantedAuthority> authorities;

    public IamUserDetails(UUID userId, String email, String passwordHash, boolean active) {
        this(userId, email, passwordHash, active, List.of());
    }

    public IamUserDetails(
            UUID userId,
            String email,
            String passwordHash,
            boolean active,
            Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.active = active;
        this.authorities = List.copyOf(authorities);
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    @Override
    @NullMarked
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
