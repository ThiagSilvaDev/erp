package com.thiagsilvadev.erpapi.financial.internal.model;

public enum AccountingEntryDirection {
    DEBIT,
    CREDIT;

    public AccountingEntryDirection opposite() {
        return this == DEBIT ? CREDIT : DEBIT;
    }
}
