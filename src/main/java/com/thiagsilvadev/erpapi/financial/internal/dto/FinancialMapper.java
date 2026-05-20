package com.thiagsilvadev.erpapi.financial.internal.dto;

import org.mapstruct.Mapper;

import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.*;
import com.thiagsilvadev.erpapi.financial.internal.model.*;

@Mapper(componentModel = "spring")
public interface FinancialMapper {

    InvoiceResponse toResponse(Invoice invoice);

    PaymentResponse toResponse(Payment payment);

    LedgerEntryResponse toResponse(LedgerEntry ledgerEntry);

    FinancialAccountResponse toResponse(FinancialAccount account);

    FinancialEntryResponse toResponse(FinancialEntry entry);
}
