package com.thiagsilvadev.erpapi.common.domain.model;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PlateValidator implements ConstraintValidator<PlateValid, String> {

    private PlateValid annotation;

    @Override
    public void initialize(PlateValid constraintAnnotation) {
        this.annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return annotation.type().getRawPattern().matcher(value).matches();
    }
}
