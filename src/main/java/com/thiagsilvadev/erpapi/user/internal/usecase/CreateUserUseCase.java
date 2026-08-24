package com.thiagsilvadev.erpapi.user.internal.usecase;

import static com.thiagsilvadev.erpapi.user.api.UserPermissions.WRITE;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.user.api.TenantAccessProvisioningFacade;
import com.thiagsilvadev.erpapi.user.internal.dto.CreateUserRequest;
import com.thiagsilvadev.erpapi.user.internal.dto.UserResponse;
import com.thiagsilvadev.erpapi.user.internal.event.UserCredentialProvisioningAfterCommitHandler;
import com.thiagsilvadev.erpapi.user.internal.event.UserEventPublisher;
import com.thiagsilvadev.erpapi.user.internal.exception.EmailAlreadyExistsException;
import com.thiagsilvadev.erpapi.user.internal.mapper.UserMapper;
import com.thiagsilvadev.erpapi.user.internal.model.User;
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;

@Component
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserEventPublisher eventPublisher;
    private final UserCredentialProvisioningAfterCommitHandler credentialProvisioningAfterCommitHandler;
    private final TenantAccessProvisioningFacade tenantAccessProvisioningFacade;

    public CreateUserUseCase(
            UserRepository userRepository,
            UserMapper userMapper,
            UserEventPublisher eventPublisher,
            UserCredentialProvisioningAfterCommitHandler credentialProvisioningAfterCommitHandler,
            TenantAccessProvisioningFacade tenantAccessProvisioningFacade) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.eventPublisher = eventPublisher;
        this.credentialProvisioningAfterCommitHandler = credentialProvisioningAfterCommitHandler;
        this.tenantAccessProvisioningFacade = tenantAccessProvisioningFacade;
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + WRITE + "') and hasAuthority('iam:role:assign')")
    public UserResponse execute(CreateUserRequest request) {
        var companyId = TenantContext.requireTenantId();
        Contact contact = Contact.create(request.email(), request.phone());

        if (userRepository.existsByContactEmailValue(contact.getEmail())) {
            throw new EmailAlreadyExistsException(contact.getEmail());
        }

        User user = User.create(request.firstName(), request.lastName(), contact, request.birthDate());
        User savedUser;
        try {
            savedUser = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(contact.getEmail());
        }
        tenantAccessProvisioningFacade.provision(savedUser.getId(), companyId, request.roleIds());
        credentialProvisioningAfterCommitHandler.provisionAfterCommit(savedUser, request.password());
        eventPublisher.userCreated(savedUser);

        return userMapper.toResponse(savedUser);
    }
}
