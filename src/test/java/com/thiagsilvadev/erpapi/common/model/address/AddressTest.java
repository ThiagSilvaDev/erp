package com.thiagsilvadev.erpapi.common.model.address;

import static com.thiagsilvadev.erpapi.support.ValueObjectAssertions.assertValueEquality;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.thiagsilvadev.erpapi.common.exception.DomainValidationException;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class AddressTest {

    @Test
    void shouldNormalizeBrazilianAddress() {
        Address address = Address.builder()
                .postalCode("01310-200")
                .street(" Paulista Avenue ")
                .number(" 1000 ")
                .complement(" ")
                .district(" Downtown ")
                .city(" Sao Paulo ")
                .region(" sp ")
                .country(Country.BR)
                .build();

        assertThat(address.getPostalCode()).isEqualTo("01310200");
        assertThat(address.getStreet()).isEqualTo("Paulista Avenue");
        assertThat(address.getNumber()).isEqualTo("1000");
        assertThat(address.getComplement()).isNull();
        assertThat(address.getDistrict()).isEqualTo("Downtown");
        assertThat(address.getCity()).isEqualTo("Sao Paulo");
        assertThat(address.getRegion()).isEqualTo("SP");
        assertThat(address.getCountry()).isEqualTo(Country.BR);
    }

    @ParameterizedTest
    @CsvSource({
            "postalCode, CEP code is required",
            "street, Street is required",
            "number, Number is required",
            "district, District is required",
            "city, City is required",
            "region, UF is required"
    })
    void shouldRejectRequiredFields(String field, String expectedMessage) {
        assertThatThrownBy(() -> buildAddressWith(builder -> {
            switch (field) {
                case "postalCode" -> builder.postalCode(null);
                case "street" -> builder.street(" ");
                case "number" -> builder.number(" ");
                case "district" -> builder.district(" ");
                case "city" -> builder.city(" ");
                case "region" -> builder.region(" ");
                default -> throw new IllegalArgumentException("Unknown field: " + field);
            }
        }))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void shouldRejectMissingCountry() {
        assertThatThrownBy(() -> buildAddressWith(builder -> builder.country(null)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Country is required");
    }

    @Test
    void shouldRejectInvalidBrazilianFederativeUnit() {
        assertThatThrownBy(() -> buildAddressWith(builder -> builder.region("XX")))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Invalid UF");
    }

    @ParameterizedTest
    @CsvSource({
            "street, 151, Street must contain at most 150 characters",
            "number, 21, Number must contain at most 20 characters",
            "complement, 101, Complement must contain at most 100 characters",
            "district, 101, District must contain at most 100 characters",
            "city, 101, City must contain at most 100 characters"
    })
    void shouldRejectFieldsAboveMaximumLength(String field, int length, String expectedMessage) {
        String invalidText = textWithLength(length);

        assertThatThrownBy(() -> buildAddressWith(builder -> {
            switch (field) {
                case "street" -> builder.street(invalidText);
                case "number" -> builder.number(invalidText);
                case "complement" -> builder.complement(invalidText);
                case "district" -> builder.district(invalidText);
                case "city" -> builder.city(invalidText);
                default -> throw new IllegalArgumentException("Unknown field: " + field);
            }
        }))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage(expectedMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567", "123456789"})
    void shouldRejectInvalidBrazilianPostalCode(String invalidPostalCode) {
        assertThatThrownBy(() -> buildAddressWith(builder -> builder.postalCode(invalidPostalCode)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("CEP code must contain 8 digits");
    }

    @Test
    void shouldNormalizeForeignAddressWithDefaultCountryRules() {
        Address address = Address.builder()
                .postalCode(" 90210-1234 ")
                .street("North Canon Drive")
                .number("1")
                .district("Beverly Hills")
                .city("Los Angeles")
                .region(" California ")
                .country(Country.US)
                .build();

        assertThat(address.getPostalCode()).isEqualTo("90210-1234");
        assertThat(address.getRegion()).isEqualTo("California");
        assertThat(address.getCountry()).isEqualTo(Country.US);
    }

    @Test
    void shouldRejectForeignAddressAboveMaximumLength() {
        String textWith31Characters = textWithLength(31);
        String textWith51Characters = textWithLength(51);

        assertThatThrownBy(() -> buildAddressWith(builder ->
                builder.country(Country.US).postalCode(textWith31Characters)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Postal code must contain at most 30 characters");

        assertThatThrownBy(() -> buildAddressWith(builder ->
                builder.country(Country.US).region(textWith51Characters)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Region/state must contain at most 50 characters");
    }

    @Test
    void shouldChangeNumberWithoutModifyingOriginalAddress() {
        Address original = addressBrazilian();

        Address changed = original.changeNumber(" 2000 ");

        assertThat(changed).isNotSameAs(original);
        assertThat(original.getNumber()).isEqualTo("1000");
        assertThat(changed.getNumber()).isEqualTo("2000");
        assertThat(changed.getPostalCode()).isEqualTo(original.getPostalCode());
        assertThat(changed.getStreet()).isEqualTo(original.getStreet());
    }

    @Test
    void shouldRejectInvalidNumberWhenChangingNumber() {
        Address address = addressBrazilian();
        String blankNumber = " ";
        String numberWith21Characters = textWithLength(21);

        assertThatThrownBy(() -> address.changeNumber(blankNumber))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Number is required");

        assertThatThrownBy(() -> address.changeNumber(numberWith21Characters))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Number must contain at most 20 characters");
    }

    @Test
    void shouldChangeComplementWithoutModifyingOriginalAddress() {
        Address original = addressBrazilian();

        Address changed = original.changeComplement(" Sala 10 ");

        assertThat(changed).isNotSameAs(original);
        assertThat(original.getComplement()).isNull();
        assertThat(changed.getComplement()).isEqualTo("Sala 10");
        assertThat(changed.getPostalCode()).isEqualTo(original.getPostalCode());
        assertThat(changed.getNumber()).isEqualTo(original.getNumber());
    }

    @Test
    void shouldNormalizeBlankComplementWhenChangingComplement() {
        Address original = addressBrazilian().changeComplement("Sala 10");

        Address changed = original.changeComplement(" ");

        assertThat(original.getComplement()).isEqualTo("Sala 10");
        assertThat(changed.getComplement()).isNull();
    }

    @Test
    void shouldRejectComplementAboveMaximumLengthWhenChangingComplement() {
        Address address = addressBrazilian();
        String complementWith101Characters = textWithLength(101);

        assertThatThrownBy(() -> address.changeComplement(complementWith101Characters))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Complement must contain at most 100 characters");
    }

    @Test
    void shouldCompareAddressesByValue() {
        Address address = addressBrazilian();
        Address equalAddress = addressBrazilian();
        Address differentAddress = address.changeNumber("2000");

        assertValueEquality(address, equalAddress, differentAddress);
    }

    private static Address addressBrazilian() {
        return addressBuilder().build();
    }

    private static void buildAddressWith(Consumer<Address.Builder> customizer) {
        Address.Builder builder = addressBuilder();
        customizer.accept(builder);
        builder.build();
    }

    private static Address.Builder addressBuilder() {
        return Address.builder()
                .postalCode("01310-200")
                .street("Paulista Avenue")
                .number("1000")
                .district("Downtown")
                .city("Sao Paulo")
                .region("SP")
                .country(Country.BR);
    }

    private static String textWithLength(int length) {
        return "a".repeat(length);
    }
}