package com.thiagsilvadev.erpapi.logistics.internal.seed;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.model.Plate;
import com.thiagsilvadev.erpapi.common.domain.model.PlateType;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.logistics.internal.model.*;
import com.thiagsilvadev.erpapi.logistics.internal.repository.DriverRepository;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentRepository;
import com.thiagsilvadev.erpapi.logistics.internal.repository.VehicleRepository;
import com.thiagsilvadev.erpapi.user.api.UserFacade;

@Component
@Profile("dev")
@Order(700)
class LogisticsDevSeeder implements ApplicationRunner {

    static final String DRIVER_LICENSE_NUMBER = "CNH-001";
    static final String DRIVER_EMAIL = "alex.driver@acme.example";
    static final String VEHICLE_PLATE = "ABC1D23";
    private static final String ACME_CNPJ = "97.986.234/0001-91";
    private static final UUID ORIGIN_WAREHOUSE_ID = namedId("logistics-seed-warehouse");
    private static final UUID PRODUCT_ID = namedId("logistics-seed-product");
    private static final UUID CUSTOMER_ID = namedId("logistics-seed-customer");

    private static final List<ShipmentSeed> SHIPMENTS = List.of(
            new ShipmentSeed(57, ShipmentStatus.DELIVERED, 30),
            new ShipmentSeed(49, ShipmentStatus.CANCELLED, null),
            new ShipmentSeed(41, ShipmentStatus.DELIVERED, 18),
            new ShipmentSeed(33, ShipmentStatus.DELIVERED, 42),
            new ShipmentSeed(28, ShipmentStatus.DELIVERED, 24),
            new ShipmentSeed(23, ShipmentStatus.DELIVERED, 32),
            new ShipmentSeed(18, ShipmentStatus.CANCELLED, null),
            new ShipmentSeed(14, ShipmentStatus.DELIVERED, 16),
            new ShipmentSeed(10, ShipmentStatus.IN_TRANSIT, null),
            new ShipmentSeed(7, ShipmentStatus.LOADED, null),
            new ShipmentSeed(4, ShipmentStatus.DISPATCH_ASSIGNED, null),
            new ShipmentSeed(1, ShipmentStatus.CREATED, null));

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final ShipmentRepository shipmentRepository;
    private final CompanyFacade companyFacade;
    private final BusinessNumberService businessNumberService;
    private final UserFacade userFacade;
    private final Clock clock;

    LogisticsDevSeeder(
            DriverRepository driverRepository,
            VehicleRepository vehicleRepository,
            ShipmentRepository shipmentRepository,
            CompanyFacade companyFacade,
            BusinessNumberService businessNumberService,
            UserFacade userFacade,
            Clock clock) {
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.shipmentRepository = shipmentRepository;
        this.companyFacade = companyFacade;
        this.businessNumberService = businessNumberService;
        this.userFacade = userFacade;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        UUID companyId = companyFacade.resolveCompany(ACME_CNPJ).id();
        UUID driverUserId = userFacade.resolveUser(DRIVER_EMAIL).id();
        Driver driver = driverRepository
                .findByCompanyIdAndLicenseNumber(companyId, DRIVER_LICENSE_NUMBER)
                .orElseGet(() -> driverRepository.save(Driver.create(companyId, driverUserId, DRIVER_LICENSE_NUMBER)));
        Plate plate = Plate.of(VEHICLE_PLATE, PlateType.MERCOSUL);
        Vehicle vehicle = vehicleRepository
                .findByCompanyIdAndPlate(companyId, plate)
                .orElseGet(() -> vehicleRepository.save(Vehicle.create(companyId, VEHICLE_PLATE, "Box truck", 1000)));
        if (shipmentRepository.countBySellerCompanyId(companyId) >= SHIPMENTS.size()) {
            return;
        }
        LocalDate today = LocalDate.now(clock);

        for (ShipmentSeed seed : SHIPMENTS) {
            Instant createdAt = today.minusDays(seed.daysAgo()).atTime(12, 0).toInstant(ZoneOffset.UTC);
            Shipment shipment = Shipment.create(
                    businessNumberService.next(BusinessNumberType.SHIPMENT),
                    namedId("logistics-seed-order-" + seed.daysAgo()),
                    companyId,
                    null,
                    CUSTOMER_ID,
                    List.of(ShipmentItem.create(PRODUCT_ID, ORIGIN_WAREHOUSE_ID, "SKU-001", "Industrial Widget", 2)));
            applyStatus(shipment, driver, vehicle, seed, createdAt);
            shipmentRepository.saveAndFlush(shipment);
            shipmentRepository.updateCreatedAtForDevSeed(shipment.getId(), createdAt);
        }
    }

    private static void applyStatus(
            Shipment shipment, Driver driver, Vehicle vehicle, ShipmentSeed seed, Instant createdAt) {
        switch (seed.status()) {
            case CREATED -> {
                // The factory already creates shipments in this status.
            }
            case CANCELLED -> shipment.cancel(createdAt.plus(6, ChronoUnit.HOURS));
            case DISPATCH_ASSIGNED -> assignDispatch(shipment, driver, vehicle);
            case LOADED -> {
                assignDispatch(shipment, driver, vehicle);
                shipment.load(createdAt.plus(4, ChronoUnit.HOURS));
            }
            case IN_TRANSIT -> {
                assignDispatch(shipment, driver, vehicle);
                shipment.load(createdAt.plus(4, ChronoUnit.HOURS));
                shipment.dispatch(createdAt.plus(8, ChronoUnit.HOURS));
            }
            case DELIVERED -> {
                assignDispatch(shipment, driver, vehicle);
                shipment.load(createdAt.plus(4, ChronoUnit.HOURS));
                shipment.dispatch(createdAt.plus(8, ChronoUnit.HOURS));
                shipment.deliver(createdAt.plus(seed.deliveryHours(), ChronoUnit.HOURS));
            }
        }
    }

    private static void assignDispatch(Shipment shipment, Driver driver, Vehicle vehicle) {
        shipment.assignDispatch(
                driver, vehicle, "São Paulo Metro", ORIGIN_WAREHOUSE_ID, "Avenida Paulista, São Paulo - SP");
    }

    private static UUID namedId(String value) {
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    static int seedShipmentCount() {
        return SHIPMENTS.size();
    }

    private record ShipmentSeed(int daysAgo, ShipmentStatus status, Integer deliveryHours) {}
}
