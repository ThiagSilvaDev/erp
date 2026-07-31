package com.thiagsilvadev.erpapi.user.internal.facade;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.user.api.UserMembershipFacade;
import com.thiagsilvadev.erpapi.user.api.UserMembershipSnapshot;
import com.thiagsilvadev.erpapi.user.api.UserMembershipStatus;
import com.thiagsilvadev.erpapi.user.internal.mapper.UserMapper;
import com.thiagsilvadev.erpapi.user.internal.model.UserMembership;
import com.thiagsilvadev.erpapi.user.internal.repository.UserMembershipRepository;

@Service
public class UserMembershipFacadeImpl implements UserMembershipFacade {

    private final UserMembershipRepository userMembershipRepository;
    private final UserMapper userMapper;

    public UserMembershipFacadeImpl(UserMembershipRepository userMembershipRepository, UserMapper userMapper) {
        this.userMembershipRepository = userMembershipRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserMembershipSnapshot> getActiveMemberships(UUID userId) {
        return userMembershipRepository.findByUserIdAndStatus(userId, UserMembershipStatus.ACTIVE).stream()
                .map(userMapper::toSnapshot)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserMembershipSnapshot getActiveMembership(UUID userId, UUID companyId) {
        return userMembershipRepository
                .findByUserIdAndCompanyId(userId, companyId)
                .filter(membership -> membership.getStatus() == UserMembershipStatus.ACTIVE)
                .map(userMapper::toSnapshot)
                .orElseThrow(() -> new ResourceNotFoundException(UserMembership.class, userId + "/" + companyId));
    }
}
