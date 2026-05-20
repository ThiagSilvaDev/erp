package com.thiagsilvadev.erpapi.logistics.internal.usecase.driver;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.logistics.internal.dto.LogisticsMapper;
import com.thiagsilvadev.erpapi.logistics.internal.dto.driver.CreateDriverRequest;
import com.thiagsilvadev.erpapi.logistics.internal.dto.driver.DriverResponse;
import com.thiagsilvadev.erpapi.logistics.internal.model.Driver;
import com.thiagsilvadev.erpapi.logistics.internal.repository.DriverRepository;

@Component
public class CreateDriverUseCase {

    private final DriverRepository driverRepository;
    private final LogisticsMapper mapper;

    public CreateDriverUseCase(DriverRepository driverRepository, LogisticsMapper mapper) {
        this.driverRepository = driverRepository;
        this.mapper = mapper;
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions).WRITE)")
    public DriverResponse execute(CreateDriverRequest request) {
        String licenseNumber = Driver.normalizeLicenseNumber(request.licenseNumber());
        if (driverRepository.existsByLicenseNumber(licenseNumber)) {
            throw new ConflictException(
                    "Driver with this license number already exists.",
                    java.util.Map.of("licenseNumber", licenseNumber));
        }
        try {
            return mapper.toResponse(
                    driverRepository.saveAndFlush(Driver.create(request.name(), licenseNumber, request.phone())));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(
                    "Driver with this license number already exists.",
                    java.util.Map.of("licenseNumber", licenseNumber));
        }
    }
}
