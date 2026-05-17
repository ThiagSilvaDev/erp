package com.thiagsilvadev.erpapi.iam.internal.usecase;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.iam.internal.dto.AuthUserResponse;
import com.thiagsilvadev.erpapi.iam.internal.dto.LoginRequest;
import com.thiagsilvadev.erpapi.iam.internal.dto.LoginResponse;
import com.thiagsilvadev.erpapi.iam.internal.exception.InvalidCredentialsException;
import com.thiagsilvadev.erpapi.iam.internal.security.IamUserDetails;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService.IssuedToken;

@Component
public class LoginUseCase {

    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;

    public LoginUseCase(JwtTokenService jwtTokenService, AuthenticationManager authenticationManager) {
        this.jwtTokenService = jwtTokenService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional(readOnly = true)
    public LoginResponse execute(LoginRequest request) {
        Authentication authentication = authenticate(request);

        if (!(authentication.getPrincipal() instanceof IamUserDetails principal)) {
            throw new InvalidCredentialsException();
        }
        IssuedToken token = jwtTokenService.issue(principal);

        return LoginResponse.bearer(token.value(), token.expiresIn(), toAuthUserResponse(principal));
    }

    private Authentication authenticate(LoginRequest request) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }
    }

    private AuthUserResponse toAuthUserResponse(IamUserDetails principal) {
        return new AuthUserResponse(principal.getUserId(), principal.getEmail(), principal.getRole());
    }
}
