package com.thiagsilvadev.erpapi.financial.internal.dto;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.*;
import com.thiagsilvadev.erpapi.financial.internal.model.*;

@Component
public class FinancialMapper {

    public InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getCustomerId(),
                invoice.getSalesOrderId(),
                invoice.getAmount(),
                invoice.getCurrencyCode(),
                invoice.getDueDate(),
                invoice.getStatus());
    }

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getInvoiceId(), payment.getAmount(), payment.getPaidAt());
    }

    public FinancialEntryResponse toResponse(FinancialEntry entry) {
        return new FinancialEntryResponse(
                entry.getId(),
                entry.getType(),
                entry.getInvoiceId(),
                entry.getAmount(),
                entry.getCurrencyCode(),
                entry.getDueDate(),
                entry.getStatus());
    }
}
