package com.thiagsilvadev.erpapi.customer.internal.dto;

public record AddressResponse(
        String postalCode,
        String street,
        String number,
        String complement,
        String district,
        String city,
        String region,
        String country) {}
