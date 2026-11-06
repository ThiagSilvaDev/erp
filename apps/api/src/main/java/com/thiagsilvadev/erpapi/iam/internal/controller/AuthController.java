package com.thiagsilvadev.erpapi.iam.internal.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.thiagsilvadev.erpapi.iam.internal.dto.LoginRequest;
import com.thiagsilvadev.erpapi.iam.internal.dto.LoginResponse;
import com.thiagsilvadev.erpapi.iam.internal.dto.SelectCompanyRequest;
import com.thiagsilvadev.erpapi.iam.internal.dto.SelectCompanyResponse;
import com.thiagsilvadev.erpapi.iam.internal.dto.SelectPlatformResponse;
import com.thiagsilvadev.erpapi.iam.internal.dto.UserCompanyResponse;
import com.thiagsilvadev.erpapi.iam.internal.usecase.ListUserCompaniesUseCase;
import com.thiagsilvadev.erpapi.iam.internal.usecase.LoginUseCase;
import com.thiagsilvadev.erpapi.iam.internal.usecase.SelectCompanyUseCase;
import com.thiagsilvadev.erpapi.iam.internal.usecase.SelectPlatformUseCase;

@RestController
@Validated
class AuthController implements AuthApi {

    private final LoginUseCase loginUseCase;
    private final SelectCompanyUseCase selectCompanyUseCase;
    private final ListUserCompaniesUseCase listUserCompaniesUseCase;
    private final SelectPlatformUseCase selectPlatformUseCase;

    AuthController(
            LoginUseCase loginUseCase,
            SelectCompanyUseCase selectCompanyUseCase,
            ListUserCompaniesUseCase listUserCompaniesUseCase,
            SelectPlatformUseCase selectPlatformUseCase) {
        this.loginUseCase = loginUseCase;
        this.selectCompanyUseCase = selectCompanyUseCase;
        this.listUserCompaniesUseCase = listUserCompaniesUseCase;
        this.selectPlatformUseCase = selectPlatformUseCase;
    }

    @Override
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return loginUseCase.execute(request);
    }

    @Override
    public SelectCompanyResponse selectCompany(
            @Valid @RequestBody SelectCompanyRequest request, @AuthenticationPrincipal Jwt jwt) {
        return selectCompanyUseCase.execute(request, jwt);
    }

    @Override
    public List<UserCompanyResponse> listCompanies(@AuthenticationPrincipal Jwt jwt) {
        return listUserCompaniesUseCase.execute(jwt);
    }

    @Override
    public SelectPlatformResponse selectPlatform(@AuthenticationPrincipal Jwt jwt) {
        return selectPlatformUseCase.execute(jwt);
    }
}
