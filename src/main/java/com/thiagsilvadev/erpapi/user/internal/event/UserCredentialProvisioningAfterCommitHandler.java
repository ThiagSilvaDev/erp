package com.thiagsilvadev.erpapi.user.internal.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.thiagsilvadev.erpapi.user.api.CredentialProvisioningFacade;
import com.thiagsilvadev.erpapi.user.api.CredentialProvisioningRequest;
import com.thiagsilvadev.erpapi.user.internal.model.User;

@Component
public class UserCredentialProvisioningAfterCommitHandler {

    private final CredentialProvisioningFacade credentialProvisioningFacade;

    public UserCredentialProvisioningAfterCommitHandler(CredentialProvisioningFacade credentialProvisioningFacade) {
        this.credentialProvisioningFacade = credentialProvisioningFacade;
    }

    public void provisionAfterCommit(User user, String rawPassword) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            provision(user, rawPassword);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                provision(user, rawPassword);
            }
        });
    }

    private void provision(User user, String rawPassword) {
        var request = new CredentialProvisioningRequest(
                user.getId(), user.getContact().getEmail(), rawPassword);

        credentialProvisioningFacade.provision(request);
    }
}
