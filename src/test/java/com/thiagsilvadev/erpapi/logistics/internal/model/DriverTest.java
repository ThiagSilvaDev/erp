package com.thiagsilvadev.erpapi.logistics.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;

class DriverTest {
    private static final UUID COMPANY_ID = UUID.randomUUID();

    @Test
    void shouldNormalizePhone() {
        Driver driver = Driver.create(COMPANY_ID, "Driver", "CNH-1", " (11) 99999-9999 ");

        assertThat(driver.getPhone()).isEqualTo("11999999999");
    }

    @Test
    void shouldRejectBlankPhone() {
        assertThatThrownBy(() -> Driver.create(COMPANY_ID, "Driver", "CNH-1", " "))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Phone cannot be blank");
    }

    @Test
    void shouldRejectInvalidPhoneLength() {
        assertThatThrownBy(() -> Driver.create(COMPANY_ID, "Driver", "CNH-1", "123456789"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Phone with area code must contain 10 or 11 digits");
    }
}
