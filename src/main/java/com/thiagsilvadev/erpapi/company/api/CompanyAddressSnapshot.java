package com.thiagsilvadev.erpapi.company.api;

import java.util.UUID;

import com.thiagsilvadev.erpapi.common.domain.model.address.Address;

public record CompanyAddressSnapshot(
        UUID companyId, String companyName, Address address, String deliveryRouteDestinationDescription) {}
