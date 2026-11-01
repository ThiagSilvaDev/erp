package com.thiagsilvadev.erpapi.financial.internal.mapper;

import org.mapstruct.Mapper;

import com.thiagsilvadev.erpapi.financial.internal.dto.account.FinancialAccountResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingaccount.AccountingAccountResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.AccountingEntryResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.entry.FinancialEntryResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.invoice.InvoiceResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.ledgerentry.LedgerEntryResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.payment.PaymentResponse;
import com.thiagsilvadev.erpapi.financial.internal.model.*;

@Mapper(componentModel = "spring")
public interface FinancialMapper {

    InvoiceResponse toResponse(Invoice invoice);

    PaymentResponse toResponse(Payment payment);

    LedgerEntryResponse toResponse(LedgerEntry ledgerEntry);

    FinancialAccountResponse toResponse(FinancialAccount account);

    FinancialEntryResponse toResponse(FinancialEntry entry);

    AccountingAccountResponse toResponse(AccountingAccount account);

    AccountingEntryResponse toResponse(AccountingEntry entry);
}
