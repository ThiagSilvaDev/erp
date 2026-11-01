package com.thiagsilvadev.erpapi.user.internal.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.user.api.UserSnapshot;
import com.thiagsilvadev.erpapi.user.api.UserStatus;
import com.thiagsilvadev.erpapi.user.internal.model.User;
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;

class UserFacadeImplTest {

    private static final UUID USER_ID = UUID.randomUUID();

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserFacadeImpl facade = new UserFacadeImpl(userRepository);

    @Test
    void shouldGetRequiredUser() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));

        UserSnapshot snapshot = facade.getUser(USER_ID);

        assertThat(snapshot.id()).isEqualTo(USER_ID);
        assertThat(snapshot.displayName()).isEqualTo("User Example");
        assertThat(snapshot.email()).isEqualTo("user@example.com");
        assertThat(snapshot.status()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void shouldNormalizeEmailWhenResolvingUser() {
        when(userRepository.findByContactEmailValue("user@example.com")).thenReturn(Optional.of(user()));

        assertThat(facade.resolveUser(" User@Example.COM ").email()).isEqualTo("user@example.com");
        verify(userRepository).findByContactEmailValue("user@example.com");
    }

    @Test
    void shouldRejectMissingUser() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.getUser(USER_ID)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldNotThrowWhenUserExists() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);

        facade.requireUser(USER_ID);

        verify(userRepository).existsById(USER_ID);
    }

    @Test
    void shouldRejectMissingUserWhenRequiring() {
        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> facade.requireUser(USER_ID)).isInstanceOf(ResourceNotFoundException.class);
    }

    private static User user() {
        User user = User.create("User", "Example", Contact.create("user@example.com", "11912345678"), null);
        ReflectionTestUtils.setField(user, "id", USER_ID);
        return user;
    }
}
