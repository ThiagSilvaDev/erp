package com.thiagsilvadev.erpapi.logistics.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.common.web.ResponseEntities;
import com.thiagsilvadev.erpapi.logistics.internal.dto.driver.CreateDriverRequest;
import com.thiagsilvadev.erpapi.logistics.internal.dto.driver.DriverResponse;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.driver.CreateDriverUseCase;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.driver.DriverQueryUseCase;

@RestController
class DriverController implements DriverApi {

    private final CreateDriverUseCase createDriverUseCase;
    private final DriverQueryUseCase driverQueryUseCase;

    DriverController(CreateDriverUseCase createDriverUseCase, DriverQueryUseCase driverQueryUseCase) {
        this.createDriverUseCase = createDriverUseCase;
        this.driverQueryUseCase = driverQueryUseCase;
    }

    @Override
    public ResponseEntity<DriverResponse> create(@Valid @RequestBody CreateDriverRequest request) {
        DriverResponse response = createDriverUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public DriverResponse getById(@PathVariable UUID id) {
        return driverQueryUseCase.getById(id);
    }

    @Override
    public Page<DriverResponse> list(Pageable pageable) {
        return driverQueryUseCase.list(pageable);
    }
}
