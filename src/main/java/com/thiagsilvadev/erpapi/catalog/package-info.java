/**
 * Owns tenant product definitions, categories, SKUs and base prices.
 *
 * <dl>
 *   <dt>Product
 *   <dd>A sellable catalog record, independent of warehouse stock.
 *   <dt>Category
 *   <dd>A tenant-owned classification of products.
 *   <dt>SKU
 *   <dd>The stable product code, unique within a company.
 *   <dt>Base price
 *   <dd>The catalog price captured when a sales item is created.
 * </dl>
 *
 * <p>Canonical definitions are maintained in {@code docs/GLOSSARY.md}.
 */
@ApplicationModule(
        allowedDependencies = {
            "common::exception",
            "common::model",
            "common::permission",
            "common::tenant",
            "common::transaction",
            "common::validation",
            "common::web",
            "company::api"
        })
package com.thiagsilvadev.erpapi.catalog;

import org.springframework.modulith.ApplicationModule;
