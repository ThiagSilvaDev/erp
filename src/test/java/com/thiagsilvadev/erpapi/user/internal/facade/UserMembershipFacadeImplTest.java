package com.thiagsilvadev.erpapi.user.internal.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.user.api.UserMembershipSnapshot;
import com.thiagsilvadev.erpapi.user.api.UserMembershipStatus;
import com.thiagsilvadev.erpapi.user.internal.mapper.UserMapper;
import com.thiagsilvadev.erpapi.user.internal.model.UserMembership;
import com.thiagsilvadev.erpapi.user.internal.repository.UserMembershipRepository;

class UserMembershipFacadeImplTest {

    private final UserMembershipRepository membershipRepository = mock(UserMembershipRepository.class);
    private final UserMapper userMapper = mock(UserMapper.class);
    private final UserMembershipFacadeImpl facade = new UserMembershipFacadeImpl(membershipRepository, userMapper);

    @Test
    void shouldListOnlyActiveMemberships() {
        UUID userId = UUID.randomUUID();
        UserMembership membership = mock(UserMembership.class);
        UserMembershipSnapshot snapshot = mock(UserMembershipSnapshot.class);
        when(membershipRepository.findByUserIdAndStatus(userId, UserMembershipStatus.ACTIVE))
                .thenReturn(List.of(membership));
        when(userMapper.toSnapshot(membership)).thenReturn(snapshot);

        assertThat(facade.getActiveMemberships(userId)).containsExactly(snapshot);
    }

    @Test
    void shouldRejectInactiveMembership() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UserMembership membership = mock(UserMembership.class);
        when(membership.getStatus()).thenReturn(UserMembershipStatus.INACTIVE);
        when(membershipRepository.findByUserIdAndCompanyId(userId, companyId)).thenReturn(Optional.of(membership));

        assertThatThrownBy(() -> facade.getActiveMembership(userId, companyId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
