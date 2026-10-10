package com.thiagsilvadev.erpapi.logistics.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;

class ShipmentStatusTest {

    @ParameterizedTest(name = "{0} -> {1}: allowed={2}")
    @MethodSource("transitions")
    void shouldEnforceTransitionGraph(ShipmentStatus source, ShipmentStatus target, boolean allowed) {
        assertThat(source.canTransitionTo(target)).isEqualTo(allowed);
        if (allowed) {
            assertThatCode(() -> source.requireTransitionTo(target)).doesNotThrowAnyException();
        } else {
            assertThatThrownBy(() -> source.requireTransitionTo(target))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("Invalid shipment status transition from " + source + " to " + target);
        }
    }

    private static Stream<Arguments> transitions() {
        Map<ShipmentStatus, Set<ShipmentStatus>> allowed = Map.of(
                ShipmentStatus.CREATED,
                EnumSet.of(ShipmentStatus.DISPATCH_ASSIGNED, ShipmentStatus.CANCELLED),
                ShipmentStatus.DISPATCH_ASSIGNED,
                EnumSet.of(ShipmentStatus.DISPATCH_ASSIGNED, ShipmentStatus.LOADED, ShipmentStatus.CANCELLED),
                ShipmentStatus.LOADED,
                EnumSet.of(ShipmentStatus.IN_TRANSIT),
                ShipmentStatus.IN_TRANSIT,
                EnumSet.of(ShipmentStatus.DELIVERED),
                ShipmentStatus.DELIVERED,
                Set.of(),
                ShipmentStatus.CANCELLED,
                Set.of());
        return EnumSet.allOf(ShipmentStatus.class).stream()
                .flatMap(source -> EnumSet.allOf(ShipmentStatus.class).stream()
                        .map(target ->
                                Arguments.of(source, target, allowed.get(source).contains(target))));
    }
}
