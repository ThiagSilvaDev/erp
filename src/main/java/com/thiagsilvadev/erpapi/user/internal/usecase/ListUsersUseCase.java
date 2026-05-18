package com.thiagsilvadev.erpapi.user.internal.usecase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.user.internal.dto.UserResponse;
import com.thiagsilvadev.erpapi.user.internal.dto.UserSearchCriteria;
import com.thiagsilvadev.erpapi.user.internal.mapper.UserMapper;
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;
import com.thiagsilvadev.erpapi.user.internal.repository.UserSpecification;

@Component
public class ListUsersUseCase {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public ListUsersUseCase(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.user.api.UserPermissions).READ)")
    public Page<UserResponse> execute(UserSearchCriteria criteria, Pageable pageable) {
        return userRepository
                .findAll(UserSpecification.withCriteria(criteria), pageable)
                .map(userMapper::toResponse);
    }
}
