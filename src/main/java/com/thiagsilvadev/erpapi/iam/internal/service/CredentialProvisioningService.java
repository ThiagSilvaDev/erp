package com.thiagsilvadev.erpapi.iam.internal.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.model.Email;
import com.thiagsilvadev.erpapi.iam.api.CredentialProvisioningFacade;
import com.thiagsilvadev.erpapi.iam.api.CredentialProvisioningRequest;
import com.thiagsilvadev.erpapi.iam.internal.exception.CredentialAlreadyExistsException;
import com.thiagsilvadev.erpapi.iam.internal.model.IamCredential;
import com.thiagsilvadev.erpapi.iam.internal.repository.IamCredentialRepository;

@Service
public class CredentialProvisioningService implements CredentialProvisioningFacade {

    private final IamCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    public CredentialProvisioningService(
            IamCredentialRepository credentialRepository, PasswordEncoder passwordEncoder) {
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void provision(CredentialProvisioningRequest request) {
        String normalizedEmail = Email.create(request.email()).getValue();

        if (credentialRepository.existsByUserId(request.userId())) {
            throw new CredentialAlreadyExistsException("userId", request.userId());
        }

        if (credentialRepository.existsByEmailValue(normalizedEmail)) {
            throw new CredentialAlreadyExistsException("email", normalizedEmail);
        }

        IamCredential credential = IamCredential.create(
                request.userId(), normalizedEmail, passwordEncoder.encode(request.rawPassword()), request.role());
        credentialRepository.save(credential);
    }
}
