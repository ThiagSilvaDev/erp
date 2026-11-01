/**
 * Provides cross-cutting contracts and utilities rather than business workflows.
 *
 * <dl>
 *   <dt>Money
 *   <dd>A monetary amount together with its currency.
 *   <dt>Address
 *   <dd>A shared value object describing a location.
 *   <dt>Tenant context
 *   <dd>The active company scope of a business request.
 *   <dt>Domain validation
 *   <dd>Shared primitives for rejecting invalid domain attributes.
 * </dl>
 *
 * <p>Canonical definitions are maintained in {@code docs/GLOSSARY.md}.
 */
@ApplicationModule
package com.thiagsilvadev.erpapi.common;

import org.springframework.modulith.ApplicationModule;
