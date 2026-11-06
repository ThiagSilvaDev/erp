/**
 * Owns credential authentication, login and access-token issuance.
 *
 * <dl>
 *   <dt>IamCredential
 *   <dd>Authentication data provisioned for a global user.
 *   <dt>Login
 *   <dd>The workflow that authenticates credentials.
 *   <dt>JWT
 *   <dd>The signed access token issued after successful authentication.
 * </dl>
 *
 * <p>Canonical definitions are maintained in {@code docs/GLOSSARY.md}.
 */
@ApplicationModule(
        allowedDependencies = {
            "common::exception",
            "common::model",
            "common::tenant",
            "common::validation",
            "common::web",
            "company::api",
            "iam.accesscontrol::api",
            "user::api"
        })
package com.thiagsilvadev.erpapi.iam;

import org.springframework.modulith.ApplicationModule;
