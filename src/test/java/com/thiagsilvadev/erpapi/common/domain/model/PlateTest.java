package com.thiagsilvadev.erpapi.common.domain.model;

import static com.thiagsilvadev.erpapi.support.ValueObjectAssertions.assertValueEquality;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;

class PlateTest {

    @Test
    void shouldSanitizeAndAcceptValidMercosulPlate() {
        Plate plate = Plate.of("ABC-1D23", PlateType.MERCOSUL);

        assertThat(plate.getValue()).isEqualTo("ABC1D23");
        assertThat(plate.getType()).isEqualTo(PlateType.MERCOSUL);
    }

    @Test
    void shouldAcceptMercosulPlateWithoutSeparator() {
        Plate plate = Plate.of("ABC1D23", PlateType.MERCOSUL);

        assertThat(plate.getValue()).isEqualTo("ABC1D23");
    }

    @Test
    void shouldAcceptMercosulPlateWithSpace() {
        Plate plate = Plate.of("ABC 1D23", PlateType.MERCOSUL);

        assertThat(plate.getValue()).isEqualTo("ABC1D23");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABC1234", "AB1D234", "ABC1D2", "ABCD123"})
    void shouldRejectInvalidMercosulPlate(String invalidPlate) {
        assertThatThrownBy(() -> Plate.of(invalidPlate, PlateType.MERCOSUL))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldAcceptBrazilLegacyPlate() {
        Plate plate = Plate.of("ABC-1234", PlateType.BRAZIL_LEGACY);

        assertThat(plate.getValue()).isEqualTo("ABC1234");
    }

    @Test
    void shouldAcceptBrazilLegacyPlateWithoutSeparator() {
        Plate plate = Plate.of("ABC1234", PlateType.BRAZIL_LEGACY);

        assertThat(plate.getValue()).isEqualTo("ABC1234");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABC1D23", "ABCD123", "AB1234"})
    void shouldRejectInvalidBrazilLegacyPlate(String invalidPlate) {
        assertThatThrownBy(() -> Plate.of(invalidPlate, PlateType.BRAZIL_LEGACY))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldAcceptUSPlate() {
        Plate plate = Plate.of("ABC 1234", PlateType.US);

        assertThat(plate.getValue()).isEqualTo("ABC1234");
    }

    @Test
    void shouldAcceptShortUSPlate() {
        Plate plate = Plate.of("AB12", PlateType.US);

        assertThat(plate.getValue()).isEqualTo("AB12");
    }

    @Test
    void shouldAcceptGenericPlate() {
        Plate plate = Plate.of("ANY-PLATE", PlateType.GENERIC);

        assertThat(plate.getValue()).isEqualTo("ANYPLATE");
    }

    @Test
    void shouldUppercasePlate() {
        Plate plate = Plate.of("abc1d23", PlateType.MERCOSUL);

        assertThat(plate.getValue()).isEqualTo("ABC1D23");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectBlankPlate(String blankPlate) {
        assertThatThrownBy(() -> Plate.of(blankPlate, PlateType.MERCOSUL))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Plate is required");
    }

    @Test
    void shouldRejectNullType() {
        assertThatThrownBy(() -> Plate.of("ABC1D23", null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Plate type is required");
    }

    @Test
    void shouldComparePlatesByValueAndType() {
        Plate plate = Plate.of("ABC1D23", PlateType.MERCOSUL);
        Plate equalPlate = Plate.of("ABC-1D23", PlateType.MERCOSUL);
        Plate differentValue = Plate.of("DEF2G34", PlateType.MERCOSUL);
        Plate differentType = Plate.of("ABC1234", PlateType.BRAZIL_LEGACY);

        assertValueEquality(plate, equalPlate, differentValue);
        assertThat(plate).isNotEqualTo(differentType);
    }

    @Test
    void shouldSanitizeRawValue() {
        assertThat(Plate.sanitize("ABC-1D23")).isEqualTo("ABC1D23");
        assertThat(Plate.sanitize("ABC 1D23")).isEqualTo("ABC1D23");
        assertThat(Plate.sanitize("abc1d23")).isEqualTo("ABC1D23");
        assertThat(Plate.sanitize("A.B.C-1D23")).isEqualTo("ABC1D23");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnNullForBlankSanitize(String blank) {
        assertThat(Plate.sanitize(blank)).isNull();
    }
}
