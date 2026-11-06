package com.thiagsilvadev.erpapi.user.internal.event;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.transaction.AfterCommit;
import com.thiagsilvadev.erpapi.user.api.CredentialProvisioningFacade;
import com.thiagsilvadev.erpapi.user.api.CredentialProvisioningRequest;
import com.thiagsilvadev.erpapi.user.internal.model.User;

@Component
public class UserCredentialProvisioningAfterCommitHandler {

    private final CredentialProvisioningFacade credentialProvisioningFacade;

    public UserCredentialProvisioningAfterCommitHandler(CredentialProvisioningFacade credentialProvisioningFacade) {
        this.credentialProvisioningFacade = credentialProvisioningFacade;
    }

    /**
     * Provisions the credential only after the user row is committed, so a rollback never leaves an
     * orphaned credential behind. The raw password is passed as an argument on purpose: it must never
     * travel through the persistent event registry.
     */
    public void provisionAfterCommit(User user, String rawPassword) {
        AfterCommit.run(() -> credentialProvisioningFacade.provision(toRequest(user, rawPassword)));
    }

    private static CredentialProvisioningRequest toRequest(User user, String rawPassword) {
        return new CredentialProvisioningRequest(user.getId(), user.getContact().getEmail(), rawPassword);
    }
}
