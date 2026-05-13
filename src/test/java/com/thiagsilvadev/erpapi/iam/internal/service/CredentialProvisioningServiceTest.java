package com.thiagsilvadev.erpapi.iam.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.thiagsilvadev.erpapi.iam.api.CredentialProvisioningRequest;
import com.thiagsilvadev.erpapi.iam.internal.exception.CredentialAlreadyExistsException;
import com.thiagsilvadev.erpapi.iam.internal.model.IamCredential;
import com.thiagsilvadev.erpapi.iam.internal.repository.IamCredentialRepository;

@ExtendWith(MockitoExtension.class)
class CredentialProvisioningServiceTest {

    @Mock
    private IamCredentialRepository credentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CredentialProvisioningService service;

    @Test
    void shouldCreateCredentialWithHashedPassword() {
        UUID userId = UUID.randomUUID();
        when(credentialRepository.existsByUserId(userId)).thenReturn(false);
        when(credentialRepository.existsByEmailValue("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");

        service.provision(new CredentialProvisioningRequest(userId, " User@Example.com ", "secret123", "admin"));

        ArgumentCaptor<IamCredential> captor = ArgumentCaptor.forClass(IamCredential.class);
        verify(credentialRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getEmail()).isEqualTo("user@example.com");
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("encoded-password");
        assertThat(captor.getValue().getRole()).isEqualTo("ADMIN");
    }

    @Test
    void shouldRejectDuplicatedUserCredential() {
        UUID userId = UUID.randomUUID();
        when(credentialRepository.existsByUserId(userId)).thenReturn(true);

        assertThatThrownBy(() -> service.provision(
                        new CredentialProvisioningRequest(userId, "user@example.com", "secret123", "ADMIN")))
                .isInstanceOf(CredentialAlreadyExistsException.class);

        verify(credentialRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicatedEmailCredential() {
        UUID userId = UUID.randomUUID();
        when(credentialRepository.existsByUserId(userId)).thenReturn(false);
        when(credentialRepository.existsByEmailValue("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.provision(
                        new CredentialProvisioningRequest(userId, "user@example.com", "secret123", "ADMIN")))
                .isInstanceOf(CredentialAlreadyExistsException.class);

        verify(credentialRepository, never()).save(any());
    }
}
