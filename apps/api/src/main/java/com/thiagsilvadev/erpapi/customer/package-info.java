/**
 * Owns tenant customer, supplier and contact-person records.
 *
 * <dl>
 *   <dt>Customer
 *   <dd>A buyer registered within a company, distinct from a global user.
 *   <dt>Supplier
 *   <dd>A tenant-owned supplier record, distinct from a company.
 *   <dt>Contact person
 *   <dd>A named contact associated with a customer.
 * </dl>
 *
 * <p>Canonical definitions are maintained in {@code docs/GLOSSARY.md}.
 */
@ApplicationModule(
        allowedDependencies = {
            "common::model",
            "common::address",
            "common::exception",
            "common::permission",
            "common::tenant",
            "common::validation",
            "common::web",
            "company::api"
        })
package com.thiagsilvadev.erpapi.customer;

import org.springframework.modulith.ApplicationModule;
