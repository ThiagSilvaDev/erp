package com.thiagsilvadev.erpapi.logistics.internal.seed;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.logistics.internal.model.Driver;
import com.thiagsilvadev.erpapi.logistics.internal.model.Vehicle;
import com.thiagsilvadev.erpapi.logistics.internal.repository.DriverRepository;
import com.thiagsilvadev.erpapi.logistics.internal.repository.VehicleRepository;

class LogisticsDevSeederTest {

    @Test
    void shouldSeedDriverAndVehicleWhenMissing() {
        DriverRepository driverRepository = mock(DriverRepository.class);
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);

        new LogisticsDevSeeder(driverRepository, vehicleRepository).run(null);

        verify(driverRepository).save(any(Driver.class));
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void shouldSkipExistingDriverAndVehicle() {
        DriverRepository driverRepository = mock(DriverRepository.class);
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        when(driverRepository.existsByLicenseNumber(LogisticsDevSeeder.DRIVER_LICENSE_NUMBER))
                .thenReturn(true);
        when(vehicleRepository.existsByPlate(LogisticsDevSeeder.VEHICLE_PLATE)).thenReturn(true);

        new LogisticsDevSeeder(driverRepository, vehicleRepository).run(null);

        verify(driverRepository, never()).save(any());
        verify(vehicleRepository, never()).save(any());
    }
}
