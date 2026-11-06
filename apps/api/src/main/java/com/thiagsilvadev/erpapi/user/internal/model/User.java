package com.thiagsilvadev.erpapi.user.internal.model;

import java.time.LocalDate;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;
import com.thiagsilvadev.erpapi.common.domain.validation.Strings;
import com.thiagsilvadev.erpapi.user.api.UserStatus;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {@UniqueConstraint(name = "uk_user_email", columnNames = "email")})
public class User extends AbstractBaseEntity {

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Embedded
    @AttributeOverride(name = "email.value", column = @Column(name = "email", nullable = false, length = 255))
    @AttributeOverride(name = "phone.value", column = @Column(name = "phone", length = 11))
    private Contact contact;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    protected User() {}

    private User(String firstName, String lastName, Contact contact, LocalDate birthDate) {
        this.firstName = Strings.required(firstName, "User first name");
        this.lastName = Strings.required(lastName, "User last name");
        this.contact = DomainAssert.requireNotNull(contact, "User contact");
        this.birthDate = birthDate;
    }

    public static User create(String firstName, String lastName, Contact contact, LocalDate birthDate) {
        return new User(firstName, lastName, contact, birthDate);
    }

    public String getDisplayName() {
        return firstName + " " + lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Contact getContact() {
        return contact;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public UserStatus getStatus() {
        return status;
    }
}
