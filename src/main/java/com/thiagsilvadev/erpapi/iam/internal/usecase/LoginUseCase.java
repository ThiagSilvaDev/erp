package com.thiagsilvadev.erpapi.iam.internal.usecase;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.iam.internal.dto.AuthCompanyResponse;
import com.thiagsilvadev.erpapi.iam.internal.dto.AuthUserResponse;
import com.thiagsilvadev.erpapi.iam.internal.dto.LoginRequest;
import com.thiagsilvadev.erpapi.iam.internal.dto.LoginResponse;
import com.thiagsilvadev.erpapi.iam.internal.dto.SelectCompanyResponse;
import com.thiagsilvadev.erpapi.iam.internal.exception.InvalidCredentialsException;
import com.thiagsilvadev.erpapi.iam.internal.security.IamUserDetails;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService.IssuedToken;
import com.thiagsilvadev.erpapi.user.api.UserMembershipFacade;
import com.thiagsilvadev.erpapi.user.api.UserMembershipSnapshot;

@Component
public class LoginUseCase {

    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;
    private final UserMembershipFacade userMembershipFacade;

    public LoginUseCase(
            JwtTokenService jwtTokenService,
            AuthenticationManager authenticationManager,
            UserMembershipFacade userMembershipFacade) {
        this.jwtTokenService = jwtTokenService;
        this.authenticationManager = authenticationManager;
        this.userMembershipFacade = userMembershipFacade;
    }

    @Transactional(readOnly = true)
    public LoginResponse execute(LoginRequest request) {
        Authentication authentication = authenticate(request);

        if (!(authentication.getPrincipal() instanceof IamUserDetails principal)) {
            throw new InvalidCredentialsException();
        }
        List<UserMembershipSnapshot> memberships = userMembershipFacade.findActiveMemberships(principal.getUserId());
        if (memberships.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        IssuedToken token = jwtTokenService.issueGlobal(principal);
        SelectCompanyResponse scopedToken =
                memberships.size() == 1 ? scopedToken(principal, memberships.getFirst()) : null;

        return LoginResponse.bearer(
                token.value(),
                token.expiresIn(),
                toAuthUserResponse(principal),
                memberships.stream().map(this::toAuthCompanyResponse).toList(),
                scopedToken);
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

    private AuthCompanyResponse toAuthCompanyResponse(UserMembershipSnapshot membership) {
        return new AuthCompanyResponse(membership.companyId(), membership.role());
    }

    private SelectCompanyResponse scopedToken(IamUserDetails principal, UserMembershipSnapshot membership) {
        IssuedToken token = jwtTokenService.issueScoped(principal, membership);
        return SelectCompanyResponse.bearer(
                token.value(), token.expiresIn(), membership.companyId(), membership.role());
    }
}
