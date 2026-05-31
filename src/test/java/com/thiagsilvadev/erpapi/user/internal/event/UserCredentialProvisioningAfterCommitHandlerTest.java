package com.thiagsilvadev.erpapi.user.internal.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.user.api.CredentialProvisioningFacade;
import com.thiagsilvadev.erpapi.user.api.CredentialProvisioningRequest;
import com.thiagsilvadev.erpapi.user.api.UserRole;
import com.thiagsilvadev.erpapi.user.internal.model.User;

@ExtendWith(MockitoExtension.class)
class UserCredentialProvisioningAfterCommitHandlerTest {

    @Mock
    private CredentialProvisioningFacade credentialProvisioningFacade;

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void shouldProvisionImmediatelyWhenTransactionSynchronizationIsNotActive() {
        User user = user();

        handler().provisionAfterCommit(user, "secret123");

        ArgumentCaptor<CredentialProvisioningRequest> requestCaptor =
                ArgumentCaptor.forClass(CredentialProvisioningRequest.class);
        verify(credentialProvisioningFacade).provision(requestCaptor.capture());
        assertProvisioningRequest(requestCaptor.getValue(), user.getId());
    }

    @Test
    void shouldProvisionOnlyAfterCommitWhenTransactionSynchronizationIsActive() {
        User user = user();
        TransactionSynchronizationManager.initSynchronization();

        handler().provisionAfterCommit(user, "secret123");

        verify(credentialProvisioningFacade, never()).provision(any());

        TransactionSynchronization synchronization =
                TransactionSynchronizationManager.getSynchronizations().getFirst();
        synchronization.afterCommit();

        ArgumentCaptor<CredentialProvisioningRequest> requestCaptor =
                ArgumentCaptor.forClass(CredentialProvisioningRequest.class);
        verify(credentialProvisioningFacade).provision(requestCaptor.capture());
        assertProvisioningRequest(requestCaptor.getValue(), user.getId());
    }

    @Test
    void shouldPropagateProvisioningFailureAfterCommit() {
        User user = user();
        TransactionSynchronizationManager.initSynchronization();
        doThrow(new IllegalStateException("credential failure"))
                .when(credentialProvisioningFacade)
                .provision(any());

        handler().provisionAfterCommit(user, "secret123");

        TransactionSynchronization synchronization =
                TransactionSynchronizationManager.getSynchronizations().getFirst();
        assertThatThrownBy(synchronization::afterCommit)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("credential failure");
    }

    private UserCredentialProvisioningAfterCommitHandler handler() {
        return new UserCredentialProvisioningAfterCommitHandler(credentialProvisioningFacade);
    }

    private static void assertProvisioningRequest(CredentialProvisioningRequest request, UUID userId) {
        assertThat(request.userId()).isEqualTo(userId);
        assertThat(request.email()).isEqualTo("user@example.com");
        assertThat(request.rawPassword()).isEqualTo("secret123");
        assertThat(request.role()).isEqualTo("ADMIN");
    }

    private static User user() {
        UUID userId = UUID.randomUUID();
        User user = User.create(
                "User",
                "Example",
                Contact.create("user@example.com", "11912345678"),
                null,
                UserRole.ADMIN,
                UUID.randomUUID());
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }
}
