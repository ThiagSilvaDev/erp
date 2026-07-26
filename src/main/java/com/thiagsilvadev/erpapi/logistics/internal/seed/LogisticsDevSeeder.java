package com.thiagsilvadev.erpapi.logistics.internal.seed;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.model.Plate;
import com.thiagsilvadev.erpapi.common.domain.model.PlateType;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.logistics.internal.model.Driver;
import com.thiagsilvadev.erpapi.logistics.internal.model.Vehicle;
import com.thiagsilvadev.erpapi.logistics.internal.repository.DriverRepository;
import com.thiagsilvadev.erpapi.logistics.internal.repository.VehicleRepository;

@Component
@Profile("dev")
@Order(700)
class LogisticsDevSeeder implements ApplicationRunner {

    static final String DRIVER_LICENSE_NUMBER = "CNH-001";
    static final String VEHICLE_PLATE = "ABC1D23";
    private static final String ACME_CNPJ = "97.986.234/0001-91";

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final CompanyFacade companyFacade;

    LogisticsDevSeeder(
            DriverRepository driverRepository, VehicleRepository vehicleRepository, CompanyFacade companyFacade) {
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.companyFacade = companyFacade;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        var companyId = companyFacade
                .findByCnpj(ACME_CNPJ)
                .orElseThrow(() -> new IllegalStateException("Seed company must exist before seeding logistics"))
                .id();
        if (!driverRepository.existsByCompanyIdAndLicenseNumber(companyId, DRIVER_LICENSE_NUMBER)) {
            driverRepository.save(Driver.create(companyId, "Alex Driver", DRIVER_LICENSE_NUMBER, "11999999999"));
        }
        if (!vehicleRepository.existsByCompanyIdAndPlate(companyId, Plate.of(VEHICLE_PLATE, PlateType.MERCOSUL))) {
            vehicleRepository.save(Vehicle.create(companyId, VEHICLE_PLATE, "Box truck", 1000));
        }
    }
}
