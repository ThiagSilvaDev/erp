package com.thiagsilvadev.erpapi.common.domain.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;

@Embeddable
public class GeoLocation {

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    protected GeoLocation() {}

    private GeoLocation(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new DomainValidationException("Latitude and longitude are required");
        }

        if (latitude.compareTo(new BigDecimal("-90")) < 0 || latitude.compareTo(new BigDecimal("90")) > 0) {
            throw new DomainValidationException("Latitude must be between -90 and 90");
        }

        if (longitude.compareTo(new BigDecimal("-180")) < 0 || longitude.compareTo(new BigDecimal("180")) > 0) {
            throw new DomainValidationException("Longitude must be between -180 and 180");
        }

        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static GeoLocation of(BigDecimal latitude, BigDecimal longitude) {
        return new GeoLocation(latitude, longitude);
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }
}
