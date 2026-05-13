package com.thiagsilvadev.erpapi.iam.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.exception.DomainValidationException;

class IamCredentialTest {

    @Test
    void shouldNormalizeCredentialEmailWithEmailValueObject() {
        IamCredential credential = IamCredential.create(UUID.randomUUID(), " User@Example.com ", "hash", "admin");

        assertThat(credential.getEmail()).isEqualTo("user@example.com");
        assertThat(credential.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void shouldRejectInvalidCredentialEmail() {
        assertThatThrownBy(() -> IamCredential.create(UUID.randomUUID(), "invalid-email", "hash", "ADMIN"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Invalid email format");
    }
}
