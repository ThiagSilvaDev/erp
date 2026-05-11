package com.thiagsilvadev.erpapi.common.model.address;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import com.thiagsilvadev.erpapi.common.validation.DomainAssert;

@Embeddable
public class Address {

    @Column(length = 30, nullable = false)
    private String postalCode;

    @Column(length = 150, nullable = false)
    private String street;

    @Column(length = 20, nullable = false)
    private String number;

    @Column(length = 100)
    private String complement;

    @Column(length = 100, nullable = false)
    private String district;

    @Column(length = 100, nullable = false)
    private String city;

    @Column(length = 50, nullable = false)
    private String region;

    @Column(length = 2, nullable = false)
    @Enumerated(EnumType.STRING)
    private Country country;

    protected Address() {}

    private Address(Builder builder) {
        this.country = processCountry(builder.country);

        this.street = processRequiredText(builder.street, "Street", 150);
        this.number = processRequiredText(builder.number, "Number", 20);
        this.complement = processOptionalText(builder.complement);
        this.district = processRequiredText(builder.district, "District", 100);
        this.city = processRequiredText(builder.city, "City", 100);

        AddressCountryRules countryRules = AddressCountryRulesRegistry.get(this.country);
        this.postalCode = countryRules.normalizePostalCode(builder.postalCode);
        this.region = countryRules.normalizeRegion(builder.region);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Address changeNumber(String newNumber) {
        return toBuilder().number(newNumber).build();
    }

    public Address changeComplement(String newComplement) {
        return toBuilder().complement(newComplement).build();
    }

    private static Country processCountry(Country rawCountry) {
        DomainAssert.notNull(rawCountry, "Country is required");
        return rawCountry;
    }

    static String processRequiredText(String rawValue, String field, int maxLength) {
        String normalizedValue = rawValue == null ? null : rawValue.trim();

        DomainAssert.notBlank(normalizedValue, field + " is required");
        DomainAssert.maxLength(
                normalizedValue, maxLength, field + " must contain at most " + maxLength + " characters");
        return normalizedValue;
    }

    private static String processOptionalText(String rawValue) {
        String normalizedValue = rawValue == null ? null : rawValue.trim();

        if (normalizedValue == null || normalizedValue.isBlank()) {
            return null;
        }

        DomainAssert.maxLength(normalizedValue, 100, "Complement must contain at most 100 characters");
        return normalizedValue;
    }

    private Builder toBuilder() {
        return builder()
                .postalCode(this.postalCode)
                .street(this.street)
                .number(this.number)
                .complement(this.complement)
                .district(this.district)
                .city(this.city)
                .region(this.region)
                .country(this.country);
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getStreet() {
        return street;
    }

    public String getNumber() {
        return number;
    }

    public String getComplement() {
        return complement;
    }

    public String getDistrict() {
        return district;
    }

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    public Country getCountry() {
        return country;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Address address)) return false;
        return Objects.equals(postalCode, address.postalCode)
                && Objects.equals(street, address.street)
                && Objects.equals(number, address.number)
                && Objects.equals(complement, address.complement)
                && Objects.equals(district, address.district)
                && Objects.equals(city, address.city)
                && Objects.equals(region, address.region)
                && Objects.equals(country, address.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postalCode, street, number, complement, district, city, region, country);
    }

    public static final class Builder {

        private String postalCode;
        private String street;
        private String number;
        private String complement;
        private String district;
        private String city;
        private String region;
        private Country country;

        private Builder() {}

        public Builder postalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public Builder street(String street) {
            this.street = street;
            return this;
        }

        public Builder number(String number) {
            this.number = number;
            return this;
        }

        public Builder complement(String complement) {
            this.complement = complement;
            return this;
        }

        public Builder district(String district) {
            this.district = district;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder region(String region) {
            this.region = region;
            return this;
        }

        public Builder country(Country country) {
            this.country = country;
            return this;
        }

        public Address build() {
            return new Address(this);
        }
    }
}
