package com.thiagsilvadev.erpapi.common.domain.model;

import static com.thiagsilvadev.erpapi.support.ValueObjectAssertions.assertValueEquality;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;

class EmailTest {

    @Test
    void shouldNormalizeEmail() {
        Email email = Email.create(" USUARIO@EXEMPLO.COM.BR ");

        assertThat(email.getValue()).isEqualTo("usuario@exemplo.com.br");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectRequiredInvalidEmail(String invalidEmail) {
        assertThatThrownBy(() -> Email.create(invalidEmail))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Email is required");
    }

    @ParameterizedTest
    @ValueSource(strings = {"usuario", "usuario@", "usuario@exemplo", "usuario@.com", "@exemplo.com"})
    void shouldRejectEmailWithInvalidFormat(String invalidFormatEmail) {
        assertThatThrownBy(() -> Email.create(invalidFormatEmail))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Invalid email format");
    }

    @Test
    void shouldRejectEmailAboveMaximumLength() {
        String emailWith256Characters = "a".repeat(244) + "@exemplo.com";

        assertThatThrownBy(() -> Email.create(emailWith256Characters))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Email must contain at most 255 characters");
    }

    @Test
    void shouldCompareEmailsByValue() {
        Email email = Email.create("usuario@exemplo.com");
        Email equalEmail = Email.create(" USUARIO@EXEMPLO.COM ");
        Email differentEmail = Email.create("outro@exemplo.com");

        assertValueEquality(email, equalEmail, differentEmail);
    }
}
