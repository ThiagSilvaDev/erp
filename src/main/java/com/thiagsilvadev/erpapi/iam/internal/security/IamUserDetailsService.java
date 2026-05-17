package com.thiagsilvadev.erpapi.iam.internal.security;

import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.thiagsilvadev.erpapi.common.domain.model.Email;
import com.thiagsilvadev.erpapi.iam.internal.model.IamCredential;
import com.thiagsilvadev.erpapi.iam.internal.repository.IamCredentialRepository;

@Service
@NullMarked
public class IamUserDetailsService implements UserDetailsService {

    private final IamCredentialRepository credentialRepository;
    private final RolePermissionResolver rolePermissionResolver;

    public IamUserDetailsService(
            IamCredentialRepository credentialRepository, RolePermissionResolver rolePermissionResolver) {
        this.credentialRepository = credentialRepository;
        this.rolePermissionResolver = rolePermissionResolver;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        String email = Email.create(username).getValue();
        IamCredential credential = credentialRepository
                .findByEmailValue(email)
                .orElseThrow(() -> new UsernameNotFoundException("Credential not found"));

        return new IamUserDetails(
                credential.getUserId(),
                credential.getEmail(),
                credential.getPasswordHash(),
                credential.getRole().name(),
                credential.isActive(),
                SecurityAuthorities.fromRole(credential.getRole().name(), rolePermissionResolver));
    }
}
