package com.thiagsilvadev.erpapi.customer.internal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ContactRequest(@NotBlank @Email String email, String phone) {}
