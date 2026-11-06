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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.thiagsilvadev.erpapi.iam.internal.exception.CredentialAlreadyExistsException;
import com.thiagsilvadev.erpapi.iam.internal.model.IamCredential;
import com.thiagsilvadev.erpapi.iam.internal.repository.IamCredentialRepository;
import com.thiagsilvadev.erpapi.user.api.CredentialProvisioningRequest;

@ExtendWith(MockitoExtension.class)
class CredentialProvisioningFacadeImplTest {

    @Mock
    private IamCredentialRepository credentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CredentialProvisioningFacadeImpl service;

    @Test
    void shouldCreateCredentialWithHashedPassword() {
        UUID userId = UUID.randomUUID();
        when(credentialRepository.existsByUserId(userId)).thenReturn(false);
        when(credentialRepository.existsByEmailValue("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");

        service.provision(new CredentialProvisioningRequest(userId, " User@Example.com ", "secret123"));

        ArgumentCaptor<IamCredential> captor = ArgumentCaptor.forClass(IamCredential.class);
        verify(credentialRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getEmail()).isEqualTo("user@example.com");
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("encoded-password");
    }

    @Test
    void shouldRejectDuplicatedUserCredential() {
        UUID userId = UUID.randomUUID();
        when(credentialRepository.existsByUserId(userId)).thenReturn(true);

        assertThatThrownBy(() ->
                        service.provision(new CredentialProvisioningRequest(userId, "user@example.com", "secret123")))
                .isInstanceOf(CredentialAlreadyExistsException.class);

        verify(credentialRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldRejectDuplicatedEmailCredential() {
        UUID userId = UUID.randomUUID();
        when(credentialRepository.existsByUserId(userId)).thenReturn(false);
        when(credentialRepository.existsByEmailValue("user@example.com")).thenReturn(true);

        assertThatThrownBy(() ->
                        service.provision(new CredentialProvisioningRequest(userId, "user@example.com", "secret123")))
                .isInstanceOf(CredentialAlreadyExistsException.class);

        verify(credentialRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldTranslateDuplicatedEmailRace() {
        UUID userId = UUID.randomUUID();
        when(credentialRepository.existsByUserId(userId)).thenReturn(false);
        when(credentialRepository.existsByEmailValue("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");
        when(credentialRepository.saveAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("uk_iam_credential_email"));

        assertThatThrownBy(() ->
                        service.provision(new CredentialProvisioningRequest(userId, "user@example.com", "secret123")))
                .isInstanceOf(CredentialAlreadyExistsException.class);
    }
}
