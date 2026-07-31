package com.thiagsilvadev.erpapi.user.internal.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.user.api.UserSnapshot;
import com.thiagsilvadev.erpapi.user.internal.mapper.UserMapper;
import com.thiagsilvadev.erpapi.user.internal.model.User;
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;

class UserFacadeImplTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserMapper userMapper = mock(UserMapper.class);
    private final UserFacadeImpl facade = new UserFacadeImpl(userRepository, userMapper);

    @Test
    void shouldGetRequiredUser() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        UserSnapshot snapshot = mock(UserSnapshot.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toSnapshot(user)).thenReturn(snapshot);

        assertThat(facade.getUser(userId)).isSameAs(snapshot);
    }

    @Test
    void shouldNormalizeEmailWhenResolvingUser() {
        User user = mock(User.class);
        UserSnapshot snapshot = mock(UserSnapshot.class);
        when(userRepository.findByContactEmailValue("user@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toSnapshot(user)).thenReturn(snapshot);

        assertThat(facade.resolveUser(" User@Example.COM ")).isSameAs(snapshot);
        verify(userRepository).findByContactEmailValue("user@example.com");
    }

    @Test
    void shouldRejectMissingUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.getUser(userId)).isInstanceOf(ResourceNotFoundException.class);
    }
}
