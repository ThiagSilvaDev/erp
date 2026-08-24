package com.thiagsilvadev.erpapi.user.internal.dto;

import com.thiagsilvadev.erpapi.user.api.UserStatus;

public record UserSearchCriteria(String email, String name, UserStatus status) {}
