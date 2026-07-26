package com.thiagsilvadev.erpapi.logistics.internal.seed;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.domain.model.Plate;
import com.thiagsilvadev.erpapi.common.domain.model.PlateType;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.logistics.internal.model.Driver;
import com.thiagsilvadev.erpapi.logistics.internal.model.Vehicle;
import com.thiagsilvadev.erpapi.logistics.internal.repository.DriverRepository;
import com.thiagsilvadev.erpapi.logistics.internal.repository.VehicleRepository;

class LogisticsDevSeederTest {
    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final Plate SEED_PLATE = Plate.of(LogisticsDevSeeder.VEHICLE_PLATE, PlateType.MERCOSUL);

    @Test
    void shouldSeedDriverAndVehicleWhenMissing() {
        DriverRepository driverRepository = mock(DriverRepository.class);
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        CompanyFacade companyFacade = companyFacade();

        new LogisticsDevSeeder(driverRepository, vehicleRepository, companyFacade).run(null);

        verify(driverRepository).save(any(Driver.class));
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void shouldSkipExistingDriverAndVehicle() {
        DriverRepository driverRepository = mock(DriverRepository.class);
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        CompanyFacade companyFacade = companyFacade();
        when(driverRepository.existsByCompanyIdAndLicenseNumber(COMPANY_ID, LogisticsDevSeeder.DRIVER_LICENSE_NUMBER))
                .thenReturn(true);
        when(vehicleRepository.existsByCompanyIdAndPlate(eq(COMPANY_ID), eq(SEED_PLATE)))
                .thenReturn(true);

        new LogisticsDevSeeder(driverRepository, vehicleRepository, companyFacade).run(null);

        verify(driverRepository, never()).save(any());
        verify(vehicleRepository, never()).save(any());
    }

    private static CompanyFacade companyFacade() {
        CompanyFacade companyFacade = mock(CompanyFacade.class);
        when(companyFacade.findByCnpj("97.986.234/0001-91")).thenReturn(Optional.of(company()));
        return companyFacade;
    }

    private static CompanySnapshot company() {
        return new CompanySnapshot(COMPANY_ID, null, null, null, null, null, null, null, null, null);
    }
}
