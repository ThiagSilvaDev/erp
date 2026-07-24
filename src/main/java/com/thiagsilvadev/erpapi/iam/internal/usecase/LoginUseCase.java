package com.thiagsilvadev.erpapi.iam.internal.usecase;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.iam.internal.dto.AuthMapper;
import com.thiagsilvadev.erpapi.iam.internal.dto.LoginRequest;
import com.thiagsilvadev.erpapi.iam.internal.dto.LoginResponse;
import com.thiagsilvadev.erpapi.iam.internal.dto.SelectCompanyResponse;
import com.thiagsilvadev.erpapi.iam.internal.exception.InvalidCredentialsException;
import com.thiagsilvadev.erpapi.iam.internal.exception.NoActiveMembershipException;
import com.thiagsilvadev.erpapi.iam.internal.security.IamUserDetails;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService.IssuedToken;
import com.thiagsilvadev.erpapi.user.api.UserMembershipFacade;
import com.thiagsilvadev.erpapi.user.api.UserMembershipSnapshot;

@Component
public class LoginUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginUseCase.class);

    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;
    private final UserMembershipFacade userMembershipFacade;
    private final AuthMapper authMapper;

    public LoginUseCase(
            JwtTokenService jwtTokenService,
            AuthenticationManager authenticationManager,
            UserMembershipFacade userMembershipFacade,
            AuthMapper authMapper) {
        this.jwtTokenService = jwtTokenService;
        this.authenticationManager = authenticationManager;
        this.userMembershipFacade = userMembershipFacade;
        this.authMapper = authMapper;
    }

    @Transactional(readOnly = true)
    public LoginResponse execute(LoginRequest request) {
        Authentication authentication = authenticate(request);

        if (!(authentication.getPrincipal() instanceof IamUserDetails principal)) {
            throw new InvalidCredentialsException();
        }
        List<UserMembershipSnapshot> memberships = userMembershipFacade.findActiveMemberships(principal.getUserId());
        if (memberships.isEmpty()) {
            log.warn("Login failed: no active memberships. userId={}", principal.getUserId());
            throw new NoActiveMembershipException();
        }

        IssuedToken token = jwtTokenService.issueGlobal(principal);
        SelectCompanyResponse scopedToken = maybeAutoScope(principal, memberships);

        return LoginResponse.bearer(
                token.value(),
                token.expiresIn(),
                authMapper.toAuthUserResponse(principal),
                memberships.stream().map(authMapper::toAuthCompanyResponse).toList(),
                scopedToken);
    }

    private Authentication authenticate(LoginRequest request) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException exception) {
            log.warn("Login failed: invalid credentials.");
            throw new InvalidCredentialsException();
        }
    }

    private SelectCompanyResponse maybeAutoScope(IamUserDetails principal, List<UserMembershipSnapshot> memberships) {
        if (memberships.size() != 1) {
            return null;
        }
        return scopedToken(principal, memberships.getFirst());
    }

    private SelectCompanyResponse scopedToken(IamUserDetails principal, UserMembershipSnapshot membership) {
        IssuedToken token = jwtTokenService.issueScoped(principal, membership);
        return SelectCompanyResponse.bearer(
                token.value(), token.expiresIn(), membership.companyId(), membership.role());
    }
}
