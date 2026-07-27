package com.thiagsilvadev.erpapi.kpi.internal.controller;

import org.springframework.web.bind.annotation.RestController;

import com.thiagsilvadev.erpapi.kpi.internal.dto.ExecutiveKpiResponse;
import com.thiagsilvadev.erpapi.kpi.internal.dto.KpiCriteria;
import com.thiagsilvadev.erpapi.kpi.internal.usecase.GetExecutiveKpisUseCase;

@RestController
class KpiController implements KpiApi {

    private final GetExecutiveKpisUseCase getExecutiveKpisUseCase;

    KpiController(GetExecutiveKpisUseCase getExecutiveKpisUseCase) {
        this.getExecutiveKpisUseCase = getExecutiveKpisUseCase;
    }

    @Override
    public ExecutiveKpiResponse getExecutiveKpis(KpiCriteria criteria) {
        return getExecutiveKpisUseCase.execute(criteria);
    }
}
