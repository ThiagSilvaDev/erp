package com.thiagsilvadev.erpapi.user.internal.usecase;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.iam.api.CredentialProvisioningFacade;
import com.thiagsilvadev.erpapi.iam.api.CredentialProvisioningRequest;
import com.thiagsilvadev.erpapi.user.internal.dto.CreateUserRequest;
import com.thiagsilvadev.erpapi.user.internal.dto.UserResponse;
import com.thiagsilvadev.erpapi.user.internal.event.UserEventPublisher;
import com.thiagsilvadev.erpapi.user.internal.exception.EmailAlreadyExistsException;
import com.thiagsilvadev.erpapi.user.internal.mapper.UserMapper;
import com.thiagsilvadev.erpapi.user.internal.model.User;
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;

@Component
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final CompanyFacade companyFacade;
    private final UserMapper userMapper;
    private final UserEventPublisher eventPublisher;
    private final CredentialProvisioningFacade credentialProvisioningFacade;

    public CreateUserUseCase(
            UserRepository userRepository,
            CompanyFacade companyFacade,
            UserMapper userMapper,
            UserEventPublisher eventPublisher,
            CredentialProvisioningFacade credentialProvisioningFacade) {
        this.userRepository = userRepository;
        this.companyFacade = companyFacade;
        this.userMapper = userMapper;
        this.eventPublisher = eventPublisher;
        this.credentialProvisioningFacade = credentialProvisioningFacade;
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.user.api.UserPermissions).WRITE)")
    public UserResponse execute(CreateUserRequest request) {
        Contact contact = Contact.create(request.email(), request.phone());

        if (userRepository.existsByContactEmailValue(contact.getEmail())) {
            throw new EmailAlreadyExistsException(contact.getEmail());
        }

        companyFacade
                .findById(request.companyId())
                .orElseThrow(() -> new ResourceNotFoundException(CompanySnapshot.class, request.companyId()));

        User user = User.create(
                request.firstName(),
                request.lastName(),
                contact,
                request.birthDate(),
                request.role(),
                request.companyId());
        User savedUser;
        try {
            savedUser = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(contact.getEmail());
        }
        credentialProvisioningFacade.provision(new CredentialProvisioningRequest(
                savedUser.getId(),
                savedUser.getContact().getEmail(),
                request.password(),
                savedUser.getRole().name()));
        eventPublisher.userCreated(savedUser);

        return userMapper.toResponse(savedUser);
    }
}
