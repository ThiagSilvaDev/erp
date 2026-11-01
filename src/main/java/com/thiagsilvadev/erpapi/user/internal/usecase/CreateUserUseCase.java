package com.thiagsilvadev.erpapi.user.internal.usecase;

import static com.thiagsilvadev.erpapi.user.api.UserPermissions.WRITE;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.user.api.TenantAccessProvisioningFacade;
import com.thiagsilvadev.erpapi.user.api.UserSnapshot;
import com.thiagsilvadev.erpapi.user.internal.dto.CreateUserRequest;
import com.thiagsilvadev.erpapi.user.internal.event.UserCredentialProvisioningAfterCommitHandler;
import com.thiagsilvadev.erpapi.user.internal.event.UserEventPublisher;
import com.thiagsilvadev.erpapi.user.internal.exception.EmailAlreadyExistsException;
import com.thiagsilvadev.erpapi.user.internal.mapper.UserMapper;
import com.thiagsilvadev.erpapi.user.internal.model.User;
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;

@Component
public class CreateUserUseCase {

    /**
     * Mirrors {@code IamPermissions.ROLE_ASSIGN}. The constant cannot be imported here because
     * {@code iam::accesscontrol} already depends on {@code user::api}; importing it would create a
     * module cycle.
     */
    private static final String ROLE_ASSIGN = "iam:role:assign";

    private final UserRepository userRepository;
    private final UserEventPublisher eventPublisher;
    private final UserCredentialProvisioningAfterCommitHandler credentialProvisioningAfterCommitHandler;
    private final TenantAccessProvisioningFacade tenantAccessProvisioningFacade;

    public CreateUserUseCase(
            UserRepository userRepository,
            UserEventPublisher eventPublisher,
            UserCredentialProvisioningAfterCommitHandler credentialProvisioningAfterCommitHandler,
            TenantAccessProvisioningFacade tenantAccessProvisioningFacade) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.credentialProvisioningAfterCommitHandler = credentialProvisioningAfterCommitHandler;
        this.tenantAccessProvisioningFacade = tenantAccessProvisioningFacade;
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + WRITE + "') and hasAuthority('" + ROLE_ASSIGN + "')")
    public UserSnapshot execute(CreateUserRequest request) {
        var companyId = TenantContext.requireTenantId();
        Contact contact = Contact.create(request.email(), request.phone());

        if (userRepository.existsByContactEmailValue(contact.getEmail())) {
            throw new EmailAlreadyExistsException(contact.getEmail());
        }

        User savedUser = save(contact, request);
        tenantAccessProvisioningFacade.provision(savedUser.getId(), companyId, request.roleIds());
        credentialProvisioningAfterCommitHandler.provisionAfterCommit(savedUser, request.password());
        eventPublisher.userCreated(savedUser);

        return UserMapper.toSnapshot(savedUser);
    }

    /**
     * The unique constraint is the only race-free guard for concurrent creations. The translated
     * exception must be rethrown: swallowing it would leave the transaction rollback-only and fail the
     * commit with {@code UnexpectedRollbackException} instead of a conflict response.
     */
    private User save(Contact contact, CreateUserRequest request) {
        User user = User.create(request.firstName(), request.lastName(), contact, request.birthDate());
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new EmailAlreadyExistsException(contact.getEmail());
        }
    }
}
