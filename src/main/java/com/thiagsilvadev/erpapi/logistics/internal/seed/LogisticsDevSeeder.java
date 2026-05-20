package com.thiagsilvadev.erpapi.logistics.internal.seed;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.logistics.internal.model.Driver;
import com.thiagsilvadev.erpapi.logistics.internal.model.Vehicle;
import com.thiagsilvadev.erpapi.logistics.internal.repository.DriverRepository;
import com.thiagsilvadev.erpapi.logistics.internal.repository.VehicleRepository;

@Component
@Profile("dev")
@Order(700)
class LogisticsDevSeeder implements ApplicationRunner {

    static final String DRIVER_LICENSE_NUMBER = "CNH-001";
    static final String VEHICLE_PLATE = "ABC1234";

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    LogisticsDevSeeder(DriverRepository driverRepository, VehicleRepository vehicleRepository) {
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!driverRepository.existsByLicenseNumber(DRIVER_LICENSE_NUMBER)) {
            driverRepository.save(Driver.create("Alex Driver", DRIVER_LICENSE_NUMBER, "11999999999"));
        }
        if (!vehicleRepository.existsByPlate(VEHICLE_PLATE)) {
            vehicleRepository.save(Vehicle.create(VEHICLE_PLATE, "Box truck", 1000));
        }
    }
}
