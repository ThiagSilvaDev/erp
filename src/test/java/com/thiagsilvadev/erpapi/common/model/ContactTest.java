package com.thiagsilvadev.erpapi.common.model;

import static com.thiagsilvadev.erpapi.support.ValueObjectAssertions.assertValueEquality;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.thiagsilvadev.erpapi.common.exception.DomainValidationException;

class ContactTest {

    @Test
    void shouldNormalizeContact() {
        Contact contact = Contact.create(" USUARIO@EXEMPLO.COM.BR ", " (11) 91234-5678 ");

        assertThat(contact.getEmail()).isEqualTo("usuario@exemplo.com.br");
        assertThat(contact.getPhone()).isEqualTo("11912345678");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldAllowPhoneMissing(String emptyPhone) {
        Contact contact = Contact.create("usuario@exemplo.com", emptyPhone);
        assertThat(contact.getPhone()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectRequiredInvalidEmail(String invalidEmail) {
        assertThatThrownBy(() -> Contact.create(invalidEmail, "11912345678"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Email is required");
    }

    @ParameterizedTest
    @ValueSource(strings = {"usuario", "usuario@", "usuario@exemplo", "usuario@.com", "@exemplo.com"})
    void shouldRejectEmailWithInvalidFormat(String invalidFormatEmail) {
        assertThatThrownBy(() -> Contact.create(invalidFormatEmail, "11912345678"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Invalid email format");
    }

    @Test
    void shouldRejectEmailAboveMaximumLength() {
        String emailWith256Characters = textWithLength() + "@exemplo.com";

        assertThatThrownBy(() -> Contact.create(emailWith256Characters, "11912345678"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Email must contain at most 255 characters");
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456789", "123456789012"})
    void shouldRejectPhoneWithInvalidDigitQuantity(String invalidLengthPhone) {
        assertThatThrownBy(() -> Contact.create("usuario@exemplo.com", invalidLengthPhone))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Phone with area code must contain 10 or 11 digits");
    }

    @Test
    void shouldChangeEmailWithoutModifyingOriginalContact() {
        Contact original = contact();

        Contact changed = original.changeEmail(" NOVO@EXEMPLO.COM ");

        assertThat(changed).isNotSameAs(original);
        assertThat(original.getEmail()).isEqualTo("usuario@exemplo.com");
        assertThat(changed.getEmail()).isEqualTo("novo@exemplo.com");
        assertThat(changed.getPhone()).isEqualTo(original.getPhone());
    }

    @Test
    void shouldChangePhoneWithoutModifyingOriginalContact() {
        Contact original = contact();

        Contact changed = original.changePhone(" (11) 1234-5678 ");

        assertThat(changed).isNotSameAs(original);
        assertThat(original.getPhone()).isEqualTo("11912345678");
        assertThat(changed.getPhone()).isEqualTo("1112345678");
        assertThat(changed.getEmail()).isEqualTo(original.getEmail());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRemovePhoneWhenChangedToBlankValue(String emptyPhone) {
        Contact original = contact();

        Contact changed = original.changePhone(emptyPhone);

        assertThat(original.getPhone()).isEqualTo("11912345678");
        assertThat(changed.getPhone()).isNull();
    }

    @Test
    void shouldCompareContactsByValue() {
        Contact contact = contact();
        Contact equalContact = contact();
        Contact differentContact = contact.changePhone("1112345678");

        assertValueEquality(contact, equalContact, differentContact);
    }

    private static Contact contact() {
        return Contact.create("usuario@exemplo.com", "11912345678");
    }

    private static String textWithLength() {
        return "a".repeat(244);
    }
}
