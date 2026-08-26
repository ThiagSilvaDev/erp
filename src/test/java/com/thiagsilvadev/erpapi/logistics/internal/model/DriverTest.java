package com.thiagsilvadev.erpapi.logistics.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.domain.exception.InvalidAttributeException;

class DriverTest {
    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    void shouldLinkUserAndNormalizeLicenseNumber() {
        Driver driver = Driver.create(COMPANY_ID, USER_ID, " cnh-1 ");

        assertThat(driver.getUserId()).isEqualTo(USER_ID);
        assertThat(driver.getLicenseNumber()).isEqualTo("CNH-1");
    }

    @Test
    void shouldRejectMissingUser() {
        assertThatThrownBy(() -> Driver.create(COMPANY_ID, null, "CNH-1"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("User ID is required");
    }

    @Test
    void shouldRejectBlankLicenseNumber() {
        assertThatThrownBy(() -> Driver.create(COMPANY_ID, USER_ID, " "))
                .isInstanceOf(InvalidAttributeException.class)
                .hasMessage("Driver license number cannot be blank");
    }
}
