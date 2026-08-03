package com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry;

import java.time.LocalDate;
import java.util.UUID;

public record AccountingEntrySearchCriteria(UUID accountId, LocalDate from, LocalDate to) {}
