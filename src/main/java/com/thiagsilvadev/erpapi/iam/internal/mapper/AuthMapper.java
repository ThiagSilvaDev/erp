package com.thiagsilvadev.erpapi.iam.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.thiagsilvadev.erpapi.iam.internal.dto.AuthUserResponse;
import com.thiagsilvadev.erpapi.iam.internal.security.IamUserDetails;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "id", source = "userId")
    AuthUserResponse toAuthUserResponse(IamUserDetails principal);
}
