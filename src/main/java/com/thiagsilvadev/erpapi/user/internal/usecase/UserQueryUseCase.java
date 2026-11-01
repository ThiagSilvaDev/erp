package com.thiagsilvadev.erpapi.user.internal.usecase;

import static com.thiagsilvadev.erpapi.user.api.UserPermissions.READ;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.user.api.TenantUserAccessFacade;
import com.thiagsilvadev.erpapi.user.api.UserSnapshot;
import com.thiagsilvadev.erpapi.user.internal.dto.UserSearchCriteria;
import com.thiagsilvadev.erpapi.user.internal.mapper.UserMapper;
import com.thiagsilvadev.erpapi.user.internal.model.User;
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;
import com.thiagsilvadev.erpapi.user.internal.repository.UserSpecification;

@Component
public class UserQueryUseCase {

    private final UserRepository userRepository;
    private final TenantUserAccessFacade tenantUserAccessFacade;

    public UserQueryUseCase(UserRepository userRepository, TenantUserAccessFacade tenantUserAccessFacade) {
        this.userRepository = userRepository;
        this.tenantUserAccessFacade = tenantUserAccessFacade;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public UserSnapshot getById(UUID id) {
        if (!tenantUserAccessFacade.hasActiveMembership(id, TenantContext.requireTenantId())) {
            throw new ResourceNotFoundException(User.class, id);
        }

        return userRepository
                .findById(id)
                .map(UserMapper::toSnapshot)
                .orElseThrow(() -> new ResourceNotFoundException(User.class, id));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<UserSnapshot> list(UserSearchCriteria criteria, Pageable pageable) {
        var allowedUserIds = tenantUserAccessFacade.getActiveUserIds(TenantContext.requireTenantId());

        return userRepository
                .findAll(UserSpecification.withCriteria(criteria, allowedUserIds), pageable)
                .map(UserMapper::toSnapshot);
    }
}
