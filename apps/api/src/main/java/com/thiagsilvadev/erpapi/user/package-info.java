/**
 * Owns global user identities and profiles, independent of tenant authorization.
 *
 * <dl>
 *   <dt>User
 *   <dd>A global identity with profile and contact details.
 *   <dt>User profile
 *   <dd>The personal details attached to a global user.
 *   <dt>UserStatus
 *   <dd>The active or inactive state of the user record.
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
package com.thiagsilvadev.erpapi.user;

import org.springframework.modulith.ApplicationModule;
