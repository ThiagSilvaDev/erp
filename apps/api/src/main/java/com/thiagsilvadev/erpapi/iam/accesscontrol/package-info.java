/**
 * Owns tenant authorization and platform-level access assignments.
 *
 * <dl>
 *   <dt>TenantMembership
 *   <dd>The association between a global user and a company.
 *   <dt>TenantRole
 *   <dd>A tenant role that groups permissions.
 *   <dt>Permission
 *   <dd>An authority required to perform an operation.
 *   <dt>AuthorizationAudit
 *   <dd>A record of an authorization-management action.
 * </dl>
 *
 * <p>Canonical definitions are maintained in {@code docs/GLOSSARY.md}.
 */
@ApplicationModule
package com.thiagsilvadev.erpapi.iam.accesscontrol;

import org.springframework.modulith.ApplicationModule;
