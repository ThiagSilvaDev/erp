package com.thiagsilvadev.erpapi.iam.internal.listener;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.iam.api.CredentialProvisioningRequest;
import com.thiagsilvadev.erpapi.iam.internal.service.CredentialProvisioningService;
import com.thiagsilvadev.erpapi.user.api.UserCreatedEvent;

@Component
public class ProvisionCredentialListener {

    private final CredentialProvisioningService credentialProvisioningService;

    public ProvisionCredentialListener(CredentialProvisioningService credentialProvisioningService) {
        this.credentialProvisioningService = credentialProvisioningService;
    }

    @ApplicationModuleListener
    public void on(UserCreatedEvent event) {
        credentialProvisioningService.provision(new CredentialProvisioningRequest(
                event.userId(),
                event.email(),
                event.rawPassword(),
                event.role()));
    }
}
