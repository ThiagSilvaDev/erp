package com.thiagsilvadev.erpapi.common.domain.model;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PlateValidator.class)
public @interface PlateValid {

    PlateType type() default PlateType.MERCOSUL;

    String message() default "Invalid plate format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
