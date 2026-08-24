package com.thiagsilvadev.erpapi.user.internal.usecase;

import static com.thiagsilvadev.erpapi.user.api.UserPermissions.READ;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.user.api.TenantUserAccessFacade;
import com.thiagsilvadev.erpapi.user.internal.dto.UserResponse;
import com.thiagsilvadev.erpapi.user.internal.dto.UserSearchCriteria;
import com.thiagsilvadev.erpapi.user.internal.mapper.UserMapper;
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;
import com.thiagsilvadev.erpapi.user.internal.repository.UserSpecification;

@Component
public class ListUsersUseCase {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TenantUserAccessFacade tenantUserAccessFacade;

    public ListUsersUseCase(
            UserRepository userRepository, UserMapper userMapper, TenantUserAccessFacade tenantUserAccessFacade) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.tenantUserAccessFacade = tenantUserAccessFacade;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<UserResponse> execute(UserSearchCriteria criteria, Pageable pageable) {
        var allowedUserIds = tenantUserAccessFacade.getActiveUserIds(TenantContext.requireTenantId());
        return userRepository
                .findAll(UserSpecification.withCriteria(criteria, allowedUserIds), pageable)
                .map(userMapper::toResponse);
    }
}
