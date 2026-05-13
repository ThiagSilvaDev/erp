package com.thiagsilvadev.erpapi.iam.internal.usecase;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.iam.internal.dto.LoginRequest;
import com.thiagsilvadev.erpapi.iam.internal.dto.LoginResponse;
import com.thiagsilvadev.erpapi.iam.internal.exception.InvalidCredentialsException;
import com.thiagsilvadev.erpapi.iam.internal.model.IamCredential;
import com.thiagsilvadev.erpapi.iam.internal.repository.IamCredentialRepository;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService.IssuedToken;

@Component
public class LoginUseCase {

    private final IamCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public LoginUseCase(
            IamCredentialRepository credentialRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService) {
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional(readOnly = true)
    public LoginResponse execute(LoginRequest request) {
        IamCredential credential = credentialRepository
                .findByEmail(IamCredential.normalizeEmail(request.email()))
                .orElseThrow(InvalidCredentialsException::new);

        if (!credential.isActive() || !passwordEncoder.matches(request.password(), credential.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        IssuedToken token = jwtTokenService.issue(credential);

        return new LoginResponse(
                token.value(), "Bearer", token.expiresIn(), credential.getUserId(), credential.getRole());
    }
}
