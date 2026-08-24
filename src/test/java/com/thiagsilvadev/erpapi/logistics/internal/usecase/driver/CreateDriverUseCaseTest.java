package com.thiagsilvadev.erpapi.logistics.internal.usecase.driver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.logistics.internal.dto.LogisticsMapper;
import com.thiagsilvadev.erpapi.logistics.internal.dto.driver.CreateDriverRequest;
import com.thiagsilvadev.erpapi.logistics.internal.dto.driver.DriverResponse;
import com.thiagsilvadev.erpapi.logistics.internal.model.Driver;
import com.thiagsilvadev.erpapi.logistics.internal.repository.DriverRepository;
import com.thiagsilvadev.erpapi.user.api.TenantUserAccessFacade;
import com.thiagsilvadev.erpapi.user.api.UserFacade;
import com.thiagsilvadev.erpapi.user.api.UserSnapshot;
import com.thiagsilvadev.erpapi.user.api.UserStatus;

class CreateDriverUseCaseTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    private DriverRepository driverRepository;
    private LogisticsMapper mapper;
    private UserFacade userFacade;
    private TenantUserAccessFacade tenantUserAccessFacade;
    private CreateDriverUseCase useCase;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(COMPANY_ID);
        driverRepository = mock(DriverRepository.class);
        mapper = mock(LogisticsMapper.class);
        userFacade = mock(UserFacade.class);
        tenantUserAccessFacade = mock(TenantUserAccessFacade.class);
        useCase = new CreateDriverUseCase(driverRepository, mapper, userFacade, tenantUserAccessFacade);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldCreateProfileForActiveDriverMembership() {
        when(userFacade.getUser(USER_ID)).thenReturn(user(UserStatus.ACTIVE));
        when(tenantUserAccessFacade.hasRole(USER_ID, COMPANY_ID, "DRIVER")).thenReturn(true);
        when(driverRepository.saveAndFlush(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));
        DriverResponse response = new DriverResponse(UUID.randomUUID(), USER_ID, "CNH-1", null);
        when(mapper.toResponse(any(Driver.class))).thenReturn(response);

        DriverResponse result = useCase.execute(new CreateDriverRequest(USER_ID, " cnh-1 "));

        assertThat(result).isSameAs(response);
        verify(driverRepository).existsByCompanyIdAndUserId(COMPANY_ID, USER_ID);
        verify(driverRepository).existsByCompanyIdAndLicenseNumber(COMPANY_ID, "CNH-1");
        verify(driverRepository)
                .saveAndFlush(argThat(driver -> driver.getUserId().equals(USER_ID)
                        && driver.getCompanyId().equals(COMPANY_ID)
                        && driver.getLicenseNumber().equals("CNH-1")));
    }

    @Test
    void shouldRejectUserWithoutDriverMembership() {
        when(userFacade.getUser(USER_ID)).thenReturn(user(UserStatus.ACTIVE));
        when(tenantUserAccessFacade.hasRole(USER_ID, COMPANY_ID, "DRIVER")).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(new CreateDriverRequest(USER_ID, "CNH-1")))
                .isInstanceOf(ConflictException.class)
                .hasMessage("User must have an active DRIVER membership in the current company.");

        verify(driverRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldRejectDuplicateUserLink() {
        when(userFacade.getUser(USER_ID)).thenReturn(user(UserStatus.ACTIVE));
        when(tenantUserAccessFacade.hasRole(USER_ID, COMPANY_ID, "DRIVER")).thenReturn(true);
        when(driverRepository.existsByCompanyIdAndUserId(COMPANY_ID, USER_ID)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(new CreateDriverRequest(USER_ID, "CNH-1")))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Driver profile already exists for this user.");

        verify(driverRepository, never()).saveAndFlush(any());
    }

    private static UserSnapshot user(UserStatus status) {
        return new UserSnapshot(
                USER_ID, "Alex Driver", "Alex", "Driver", "alex@example.com", "11999999999", null, status);
    }
}
