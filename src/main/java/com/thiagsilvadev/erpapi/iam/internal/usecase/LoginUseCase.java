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

import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AccessControlFacade;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantAuthorizationSnapshot;
import com.thiagsilvadev.erpapi.iam.internal.dto.AuthCompanyResponse;
import com.thiagsilvadev.erpapi.iam.internal.dto.AuthMapper;
import com.thiagsilvadev.erpapi.iam.internal.dto.AuthRoleResponse;
import com.thiagsilvadev.erpapi.iam.internal.dto.LoginRequest;
import com.thiagsilvadev.erpapi.iam.internal.dto.LoginResponse;
import com.thiagsilvadev.erpapi.iam.internal.dto.SelectCompanyResponse;
import com.thiagsilvadev.erpapi.iam.internal.exception.InvalidCredentialsException;
import com.thiagsilvadev.erpapi.iam.internal.exception.NoActiveMembershipException;
import com.thiagsilvadev.erpapi.iam.internal.security.IamUserDetails;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService.IssuedToken;

@Component
public class LoginUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginUseCase.class);

    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;
    private final AccessControlFacade accessControlFacade;
    private final CompanyFacade companyFacade;
    private final AuthMapper authMapper;

    public LoginUseCase(
            JwtTokenService jwtTokenService,
            AuthenticationManager authenticationManager,
            AccessControlFacade accessControlFacade,
            CompanyFacade companyFacade,
            AuthMapper authMapper) {
        this.jwtTokenService = jwtTokenService;
        this.authenticationManager = authenticationManager;
        this.accessControlFacade = accessControlFacade;
        this.companyFacade = companyFacade;
        this.authMapper = authMapper;
    }

    @Transactional(readOnly = true)
    public LoginResponse execute(LoginRequest request) {
        Authentication authentication = authenticate(request);

        if (!(authentication.getPrincipal() instanceof IamUserDetails principal)) {
            throw new InvalidCredentialsException();
        }
        List<TenantAuthorizationSnapshot> memberships = accessControlFacade.getActiveMemberships(principal.getUserId());
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
                memberships.stream().map(this::toAuthCompanyResponse).toList(),
                scopedToken);
    }

    private AuthCompanyResponse toAuthCompanyResponse(TenantAuthorizationSnapshot membership) {
        var company = companyFacade.getCompany(membership.companyId());

        return new AuthCompanyResponse(
                membership.companyId(),
                company.tradeName(),
                company.cnpj() != null ? company.cnpj().getNumber() : null,
                membership.roles().stream()
                        .map(role -> new AuthRoleResponse(role.id(), role.code(), role.name()))
                        .toList());
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

    private SelectCompanyResponse maybeAutoScope(
            IamUserDetails principal, List<TenantAuthorizationSnapshot> memberships) {
        if (memberships.size() != 1) {
            return null;
        }
        return scopedToken(principal, memberships.getFirst());
    }

    private SelectCompanyResponse scopedToken(IamUserDetails principal, TenantAuthorizationSnapshot membership) {
        IssuedToken token = jwtTokenService.issueScoped(principal, membership);
        return SelectCompanyResponse.bearer(
                token.value(),
                token.expiresIn(),
                membership.companyId(),
                membership.roles().stream()
                        .map(role -> new AuthRoleResponse(role.id(), role.code(), role.name()))
                        .toList(),
                membership.permissions());
    }
}
