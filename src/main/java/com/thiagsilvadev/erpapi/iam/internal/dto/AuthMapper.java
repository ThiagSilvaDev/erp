package com.thiagsilvadev.erpapi.iam.internal.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.thiagsilvadev.erpapi.iam.internal.security.IamUserDetails;
import com.thiagsilvadev.erpapi.user.api.UserMembershipSnapshot;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "id", source = "userId")
    AuthUserResponse toAuthUserResponse(IamUserDetails principal);

    AuthCompanyResponse toAuthCompanyResponse(UserMembershipSnapshot membership);
}
