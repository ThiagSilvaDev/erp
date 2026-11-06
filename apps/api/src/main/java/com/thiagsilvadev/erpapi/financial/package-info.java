/**
 * Owns billing, settlement, financial-account movements and accounting postings.
 *
 * <dl>
 *   <dt>FinancialObligation
 *   <dd>An amount receivable or payable, with a due date and settlement state.
 *   <dt>CashMovement
 *   <dd>An income, expense, transfer or adjustment of a financial account.
 *   <dt>Invoice
 *   <dd>A billing record with an amount, currency, due date and billing party.
 *   <dt>AccountingEntry
 *   <dd>A dated debit or credit posting to an accounting account.
 * </dl>
 *
 * <p>Canonical definitions are maintained in {@code docs/GLOSSARY.md}.
 */
@ApplicationModule(
        allowedDependencies = {
            "common::model",
            "common::exception",
            "common::numbering",
            "common::permission",
            "common::reporting",
            "common::tenant",
            "common::validation",
            "common::web",
            "company::api",
            "customer::api",
            "sales::api"
        })
package com.thiagsilvadev.erpapi.financial;

import org.springframework.modulith.ApplicationModule;
