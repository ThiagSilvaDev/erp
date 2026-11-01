package com.thiagsilvadev.erpapi.user.internal.mapper;

import com.thiagsilvadev.erpapi.user.api.UserSnapshot;
import com.thiagsilvadev.erpapi.user.internal.model.User;

public final class UserMapper {

    private UserMapper() {}

    public static UserSnapshot toSnapshot(User user) {
        return new UserSnapshot(
                user.getId(),
                user.getDisplayName(),
                user.getFirstName(),
                user.getLastName(),
                user.getContact().getEmail(),
                user.getContact().getPhone(),
                user.getBirthDate(),
                user.getStatus());
    }
}
