package com.thiagsilvadev.erpapi.user.internal.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.user.api.UserMembershipFacade;
import com.thiagsilvadev.erpapi.user.api.UserMembershipSnapshot;
import com.thiagsilvadev.erpapi.user.api.UserMembershipStatus;
import com.thiagsilvadev.erpapi.user.internal.mapper.UserMapper;
import com.thiagsilvadev.erpapi.user.internal.repository.UserMembershipRepository;

@Service
public class UserMembershipService implements UserMembershipFacade {

    private final UserMembershipRepository userMembershipRepository;
    private final UserMapper userMapper;

    public UserMembershipService(UserMembershipRepository userMembershipRepository, UserMapper userMapper) {
        this.userMembershipRepository = userMembershipRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserMembershipSnapshot> findActiveMemberships(UUID userId) {
        return userMembershipRepository.findByUserIdAndStatus(userId, UserMembershipStatus.ACTIVE).stream()
                .map(userMapper::toSnapshot)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserMembershipSnapshot> findMembership(UUID userId, UUID companyId) {
        return userMembershipRepository
                .findByUserIdAndCompanyId(userId, companyId)
                .map(userMapper::toSnapshot);
    }
}
