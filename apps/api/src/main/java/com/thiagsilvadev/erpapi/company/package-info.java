/**
 * Owns business organizations and their company details.
 *
 * <dl>
 *   <dt>Company
 *   <dd>A business organization that scopes tenant-owned records.
 *   <dt>Company type
 *   <dd>Factory or correspondent classification used by sales eligibility rules.
 *   <dt>Company contact
 *   <dd>Contact details associated with a company.
 *   <dt>Company address
 *   <dd>The company location used by business workflows.
 * </dl>
 *
 * <p>Canonical definitions are maintained in {@code docs/GLOSSARY.md}.
 */
@ApplicationModule(
        allowedDependencies = {
            "common::model",
            "common::address",
            "common::exception",
            "common::numbering",
            "common::permission",
            "common::security",
            "common::tenant",
            "common::transaction",
            "common::validation",
            "common::web"
        })
package com.thiagsilvadev.erpapi.company;

import org.springframework.modulith.ApplicationModule;
