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
import com.thiagsilvadev.erpapi.logistics.internal.usecase.driver.GetDriverByIdUseCase;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.driver.ListDriversUseCase;

@RestController
class DriverController implements DriverApi {

    private final CreateDriverUseCase createDriverUseCase;
    private final GetDriverByIdUseCase getDriverByIdUseCase;
    private final ListDriversUseCase listDriversUseCase;

    DriverController(
            CreateDriverUseCase createDriverUseCase,
            GetDriverByIdUseCase getDriverByIdUseCase,
            ListDriversUseCase listDriversUseCase) {
        this.createDriverUseCase = createDriverUseCase;
        this.getDriverByIdUseCase = getDriverByIdUseCase;
        this.listDriversUseCase = listDriversUseCase;
    }

    @Override
    public ResponseEntity<DriverResponse> createDriver(@Valid @RequestBody CreateDriverRequest request) {
        DriverResponse response = createDriverUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public DriverResponse getDriver(@PathVariable UUID id) {
        return getDriverByIdUseCase.execute(id);
    }

    @Override
    public Page<DriverResponse> listDrivers(Pageable pageable) {
        return listDriversUseCase.execute(pageable);
    }
}
