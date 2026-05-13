package com.thiagsilvadev.erpapi.user.internal.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.model.Contact;
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
    private final CredentialProvisioningFacade credentialProvisioningFacade;
    private final UserMapper userMapper;
    private final UserEventPublisher eventPublisher;

    public CreateUserUseCase(
            UserRepository userRepository,
            CompanyFacade companyFacade,
            CredentialProvisioningFacade credentialProvisioningFacade,
            UserMapper userMapper,
            UserEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.companyFacade = companyFacade;
        this.credentialProvisioningFacade = credentialProvisioningFacade;
        this.userMapper = userMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public UserResponse execute(CreateUserRequest request) {
        Contact contact = Contact.create(request.email(), request.phone());

        if (userRepository.existsByContactEmail(contact.getEmail())) {
            throw new EmailAlreadyExistsException(contact.getEmail());
        }

        companyFacade
                .findById(request.companyId())
                .orElseThrow(() -> new ResourceNotFoundException(CompanySnapshot.class, request.companyId()));

        User user = userMapper.toEntity(request, contact);
        User savedUser = userRepository.save(user);
        credentialProvisioningFacade.provision(new CredentialProvisioningRequest(
                savedUser.getId(),
                savedUser.getContact().getEmail(),
                request.password(),
                savedUser.getRole().name()));

        eventPublisher.userCreated(savedUser);

        return userMapper.toResponse(savedUser);
    }
}
