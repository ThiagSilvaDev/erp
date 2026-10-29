package com.thiagsilvadev.erpapi.iam.internal.security;

import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.iam.internal.model.CredentialStatus;
import com.thiagsilvadev.erpapi.iam.internal.repository.IamCredentialRepository;

public class CredentialAuthenticationService {
    private final IamCredentialRepository repository;

    public CredentialAuthenticationService(IamCredentialRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public boolean isActive(UUID userId) {
        return repository.existsByUserIdAndStatus(userId, CredentialStatus.ACTIVE);
    }
}
