package com.thiagsilvadev.erpapi.company.internal.dto;

import jakarta.validation.constraints.Email;

public record ContactRequest(@Email String email, String phone) {}
