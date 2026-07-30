package com.thiagsilvadev.erpapi.logistics.internal.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.logistics.internal.model.Driver;
import com.thiagsilvadev.erpapi.logistics.internal.model.Shipment;
import com.thiagsilvadev.erpapi.logistics.internal.model.ShipmentStatus;
import com.thiagsilvadev.erpapi.logistics.internal.model.Vehicle;
import com.thiagsilvadev.erpapi.logistics.internal.repository.DriverRepository;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentRepository;
import com.thiagsilvadev.erpapi.logistics.internal.repository.VehicleRepository;
import com.thiagsilvadev.erpapi.user.api.UserFacade;
import com.thiagsilvadev.erpapi.user.api.UserSnapshot;

class LogisticsDevSeederTest {
    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-27T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void shouldSeedShipmentsAcrossStatusesAndDates() {
        DriverRepository driverRepository = mock(DriverRepository.class);
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        BusinessNumberService businessNumberService = mock(BusinessNumberService.class);
        UUID driverUserId = UUID.randomUUID();
        Driver driver = Driver.create(COMPANY_ID, driverUserId, LogisticsDevSeeder.DRIVER_LICENSE_NUMBER);
        Vehicle vehicle = Vehicle.create(COMPANY_ID, LogisticsDevSeeder.VEHICLE_PLATE, "Box truck", 1000);
        when(driverRepository.findByCompanyIdAndLicenseNumber(any(), any())).thenReturn(Optional.empty());
        when(vehicleRepository.findByCompanyIdAndPlate(any(), any())).thenReturn(Optional.empty());
        when(driverRepository.save(any())).thenReturn(driver);
        when(vehicleRepository.save(any())).thenReturn(vehicle);
        when(businessNumberService.next(any())).thenReturn(1L);
        when(shipmentRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            Shipment shipment = invocation.getArgument(0);
            ReflectionTestUtils.setField(shipment, "id", UUID.randomUUID());
            return shipment;
        });

        new LogisticsDevSeeder(
                        driverRepository,
                        vehicleRepository,
                        shipmentRepository,
                        companyFacade(),
                        businessNumberService,
                        userFacade(driverUserId),
                        CLOCK)
                .run(null);

        ArgumentCaptor<Shipment> captor = ArgumentCaptor.forClass(Shipment.class);
        verify(shipmentRepository, times(LogisticsDevSeeder.seedShipmentCount()))
                .saveAndFlush(captor.capture());
        verify(shipmentRepository, times(LogisticsDevSeeder.seedShipmentCount()))
                .updateCreatedAtForDevSeed(any(), any());
        assertThat(captor.getAllValues())
                .extracting(Shipment::getStatus)
                .contains(
                        ShipmentStatus.CREATED,
                        ShipmentStatus.DISPATCH_ASSIGNED,
                        ShipmentStatus.LOADED,
                        ShipmentStatus.IN_TRANSIT,
                        ShipmentStatus.DELIVERED,
                        ShipmentStatus.CANCELLED);
    }

    @Test
    void shouldSkipShipmentsWhenFullDatasetAlreadyExists() {
        DriverRepository driverRepository = mock(DriverRepository.class);
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        UUID driverUserId = UUID.randomUUID();
        Driver driver = Driver.create(COMPANY_ID, driverUserId, LogisticsDevSeeder.DRIVER_LICENSE_NUMBER);
        Vehicle vehicle = Vehicle.create(COMPANY_ID, LogisticsDevSeeder.VEHICLE_PLATE, "Box truck", 1000);
        when(driverRepository.findByCompanyIdAndLicenseNumber(any(), any())).thenReturn(Optional.of(driver));
        when(vehicleRepository.findByCompanyIdAndPlate(any(), any())).thenReturn(Optional.of(vehicle));
        when(shipmentRepository.countBySellerCompanyId(COMPANY_ID))
                .thenReturn((long) LogisticsDevSeeder.seedShipmentCount());

        new LogisticsDevSeeder(
                        driverRepository,
                        vehicleRepository,
                        shipmentRepository,
                        companyFacade(),
                        mock(BusinessNumberService.class),
                        userFacade(driverUserId),
                        CLOCK)
                .run(null);

        verify(shipmentRepository, never()).save(any());
    }

    private static CompanyFacade companyFacade() {
        CompanyFacade companyFacade = mock(CompanyFacade.class);
        when(companyFacade.findByCnpj("97.986.234/0001-91")).thenReturn(Optional.of(company()));
        return companyFacade;
    }

    private static CompanySnapshot company() {
        return new CompanySnapshot(COMPANY_ID, null, null, null, null, null, null, null, null, null);
    }

    private static UserFacade userFacade(UUID userId) {
        UserFacade userFacade = mock(UserFacade.class);
        when(userFacade.findByEmail(LogisticsDevSeeder.DRIVER_EMAIL))
                .thenReturn(Optional.of(new UserSnapshot(
                        userId,
                        "Alex Driver",
                        "Alex",
                        "Driver",
                        LogisticsDevSeeder.DRIVER_EMAIL,
                        "11999999999",
                        null,
                        null,
                        null,
                        COMPANY_ID)));
        return userFacade;
    }
}
