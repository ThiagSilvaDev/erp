package com.thiagsilvadev.erpapi.user.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.thiagsilvadev.erpapi.user.api.UserMembershipSnapshot;
import com.thiagsilvadev.erpapi.user.api.UserSnapshot;
import com.thiagsilvadev.erpapi.user.internal.dto.UserResponse;
import com.thiagsilvadev.erpapi.user.internal.model.User;
import com.thiagsilvadev.erpapi.user.internal.model.UserMembership;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "email", source = "contact.email")
    @Mapping(target = "phone", source = "contact.phone")
    UserResponse toResponse(User user);

    @Mapping(target = "email", source = "contact.email")
    @Mapping(target = "phone", source = "contact.phone")
    UserSnapshot toSnapshot(User user);

    UserMembershipSnapshot toSnapshot(UserMembership userMembership);
}
