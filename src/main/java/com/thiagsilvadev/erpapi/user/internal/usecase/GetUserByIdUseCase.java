package com.thiagsilvadev.erpapi.user.internal.usecase;

import static com.thiagsilvadev.erpapi.user.api.UserPermissions.READ;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.user.api.TenantUserAccessFacade;
import com.thiagsilvadev.erpapi.user.internal.dto.UserResponse;
import com.thiagsilvadev.erpapi.user.internal.mapper.UserMapper;
import com.thiagsilvadev.erpapi.user.internal.model.User;
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;

@Component
public class GetUserByIdUseCase {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TenantUserAccessFacade tenantUserAccessFacade;

    public GetUserByIdUseCase(
            UserRepository userRepository, UserMapper userMapper, TenantUserAccessFacade tenantUserAccessFacade) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.tenantUserAccessFacade = tenantUserAccessFacade;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public UserResponse execute(UUID id) {
        if (!tenantUserAccessFacade.hasActiveMembership(id, TenantContext.requireTenantId())) {
            throw new ResourceNotFoundException(User.class, id);
        }
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(User.class, id));

        return userMapper.toResponse(user);
    }
}
