package com.thiagsilvadev.erpapi.user.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.thiagsilvadev.erpapi.common.model.Contact;
import com.thiagsilvadev.erpapi.user.api.UserSnapshot;
import com.thiagsilvadev.erpapi.user.internal.dto.CreateUserRequest;
import com.thiagsilvadev.erpapi.user.internal.dto.UserResponse;
import com.thiagsilvadev.erpapi.user.internal.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "email", source = "contact.email")
    @Mapping(target = "phone", source = "contact.phone")
    UserResponse toResponse(User user);

    @Mapping(target = "email", source = "contact.email")
    @Mapping(target = "phone", source = "contact.phone")
    UserSnapshot toSnapshot(User user);

    default User toEntity(CreateUserRequest request, Contact contact) {
        if (request == null) {
            return null;
        }
        return User.create(
                request.firstName(),
                request.lastName(),
                contact,
                request.birthDate(),
                request.role(),
                request.companyId());
    }
}
