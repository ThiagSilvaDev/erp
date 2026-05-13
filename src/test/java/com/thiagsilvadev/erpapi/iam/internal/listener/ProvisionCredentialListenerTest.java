package com.thiagsilvadev.erpapi.iam.internal.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thiagsilvadev.erpapi.iam.api.CredentialProvisioningRequest;
import com.thiagsilvadev.erpapi.iam.internal.service.CredentialProvisioningService;
import com.thiagsilvadev.erpapi.user.api.UserCreatedEvent;

@ExtendWith(MockitoExtension.class)
class ProvisionCredentialListenerTest {

    @Mock
    private CredentialProvisioningService credentialProvisioningService;

    @InjectMocks
    private ProvisionCredentialListener listener;

    @Test
    void shouldProvisionCredentialWhenUserIsCreated() {
        UUID userId = UUID.randomUUID();
        UserCreatedEvent event =
                new UserCreatedEvent(userId, "user@example.com", UUID.randomUUID(), "secret123", "ADMIN");

        listener.on(event);

        ArgumentCaptor<CredentialProvisioningRequest> captor =
                ArgumentCaptor.forClass(CredentialProvisioningRequest.class);
        verify(credentialProvisioningService).provision(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(userId);
        assertThat(captor.getValue().email()).isEqualTo("user@example.com");
        assertThat(captor.getValue().rawPassword()).isEqualTo("secret123");
        assertThat(captor.getValue().role()).isEqualTo("ADMIN");
    }
}
