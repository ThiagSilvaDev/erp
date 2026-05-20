package com.thiagsilvadev.erpapi.logistics.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.common.web.ResponseEntities;
import com.thiagsilvadev.erpapi.logistics.internal.dto.*;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.*;

@RestController
class VehicleController implements VehicleApi {

    private final CreateVehicleUseCase createVehicleUseCase;
    private final GetVehicleByIdUseCase getVehicleByIdUseCase;
    private final ListVehiclesUseCase listVehiclesUseCase;

    VehicleController(
            CreateVehicleUseCase createVehicleUseCase,
            GetVehicleByIdUseCase getVehicleByIdUseCase,
            ListVehiclesUseCase listVehiclesUseCase) {
        this.createVehicleUseCase = createVehicleUseCase;
        this.getVehicleByIdUseCase = getVehicleByIdUseCase;
        this.listVehiclesUseCase = listVehiclesUseCase;
    }

    @Override
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody CreateVehicleRequest request) {
        VehicleResponse response = createVehicleUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public VehicleResponse getVehicle(@PathVariable UUID id) {
        return getVehicleByIdUseCase.execute(id);
    }

    @Override
    public Page<VehicleResponse> listVehicles(Pageable pageable) {
        return listVehiclesUseCase.execute(pageable);
    }
}
