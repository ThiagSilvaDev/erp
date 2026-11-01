package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.financial.internal.dto.financialobligation.FinancialObligationResponse;
import com.thiagsilvadev.erpapi.financial.internal.usecase.financialobligation.FinancialObligationQueryUseCase;

@RestController
class FinancialObligationController implements FinancialObligationApi {

    private final FinancialObligationQueryUseCase financialObligationQueryUseCase;

    FinancialObligationController(FinancialObligationQueryUseCase financialObligationQueryUseCase) {
        this.financialObligationQueryUseCase = financialObligationQueryUseCase;
    }

    @Override
    public FinancialObligationResponse getFinancialObligation(@PathVariable UUID id) {
        return financialObligationQueryUseCase.getById(id);
    }

    @Override
    public Page<FinancialObligationResponse> listFinancialObligations(Pageable pageable) {
        return financialObligationQueryUseCase.list(pageable);
    }
}
