package com.thiagsilvadev.erpapi.company.internal.dto;

import java.util.UUID;

public record CompanyAddressResponse(
        UUID companyId, String companyName, AddressResponse address, String deliveryRouteDestinationDescription) {}
