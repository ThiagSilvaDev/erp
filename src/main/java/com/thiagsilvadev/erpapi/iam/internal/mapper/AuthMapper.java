package com.thiagsilvadev.erpapi.iam.internal.mapper;

import com.thiagsilvadev.erpapi.iam.internal.dto.AuthUserResponse;
import com.thiagsilvadev.erpapi.iam.internal.security.IamUserDetails;

public final class AuthMapper {

    private AuthMapper() {}

    public static AuthUserResponse toAuthUserResponse(IamUserDetails principal) {
        return new AuthUserResponse(principal.getUserId(), principal.getEmail());
    }
}
