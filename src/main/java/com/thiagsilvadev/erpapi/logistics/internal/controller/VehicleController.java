package com.thiagsilvadev.erpapi.logistics.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.common.web.ResponseEntities;
import com.thiagsilvadev.erpapi.logistics.internal.dto.vehicle.CreateVehicleRequest;
import com.thiagsilvadev.erpapi.logistics.internal.dto.vehicle.VehicleResponse;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.vehicle.CreateVehicleUseCase;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.vehicle.VehicleQueryUseCase;

@RestController
class VehicleController implements VehicleApi {

    private final CreateVehicleUseCase createVehicleUseCase;
    private final VehicleQueryUseCase vehicleQueryUseCase;

    VehicleController(CreateVehicleUseCase createVehicleUseCase, VehicleQueryUseCase vehicleQueryUseCase) {
        this.createVehicleUseCase = createVehicleUseCase;
        this.vehicleQueryUseCase = vehicleQueryUseCase;
    }

    @Override
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody CreateVehicleRequest request) {
        VehicleResponse response = createVehicleUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public VehicleResponse getVehicle(@PathVariable UUID id) {
        return vehicleQueryUseCase.getById(id);
    }

    @Override
    public Page<VehicleResponse> listVehicles(Pageable pageable) {
        return vehicleQueryUseCase.list(pageable);
    }
}
