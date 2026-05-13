package com.thiagsilvadev.erpapi.iam.internal.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.thiagsilvadev.erpapi.iam.internal.model.IamCredential;
import com.thiagsilvadev.erpapi.iam.internal.repository.IamCredentialRepository;

@ExtendWith(MockitoExtension.class)
class IamUserDetailsServiceTest {

    @Mock
    private IamCredentialRepository credentialRepository;

    @InjectMocks
    private IamUserDetailsService userDetailsService;

    @Test
    void shouldLoadCredentialByNormalizedEmail() {
        UUID userId = UUID.randomUUID();
        IamCredential credential = IamCredential.create(userId, "user@example.com", "hash", "ADMIN");
        when(credentialRepository.findByEmailValue("user@example.com")).thenReturn(Optional.of(credential));

        IamUserDetails userDetails = (IamUserDetails) userDetailsService.loadUserByUsername(" User@Example.com ");

        assertThat(userDetails.getUserId()).isEqualTo(userId);
        assertThat(userDetails.getUsername()).isEqualTo("user@example.com");
        assertThat(userDetails.getEmail()).isEqualTo("user@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("hash");
        assertThat(userDetails.getRole()).isEqualTo("ADMIN");
        assertThat(userDetails.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactly("ROLE_ADMIN");
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void shouldThrowWhenCredentialDoesNotExist() {
        when(credentialRepository.findByEmailValue("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void shouldDisableInactiveCredential() {
        IamCredential credential = IamCredential.create(UUID.randomUUID(), "user@example.com", "hash", "MANAGER");
        credential.inactivate();
        when(credentialRepository.findByEmailValue("user@example.com")).thenReturn(Optional.of(credential));

        IamUserDetails userDetails = (IamUserDetails) userDetailsService.loadUserByUsername("user@example.com");

        assertThat(userDetails.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactly("ROLE_MANAGER");
        assertThat(userDetails.isEnabled()).isFalse();
    }
}
