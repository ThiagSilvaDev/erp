# ADR 0003: Stateless JWTs with tenant and platform contexts

Status: Accepted (retrospective).

## Context

A global user can belong to multiple companies. Tenant business permissions and
platform administration need separate authorization contexts.

## Decision

Authenticate credentials and issue a global JWT. Exchange it for a tenant-scoped
or platform token after validating the relevant membership/assignment. Use
stateless bearer authentication, method-level permissions and persisted
membership/assignment authorization versions on contextual requests.

## Consequences

HTTP login sessions are not stored, but contextual requests still consult
persisted authorization. Changes can invalidate old contextual tokens without
waiting for expiry. Global-token revocation, refresh tokens and per-token logout
are not implemented. HS256 uses a shared secret, so issuer validation and key
rotation behavior must be assessed from the actual configuration rather than
assumed from the token format.

## References

[Security](../SECURITY.md),
[token issuance](../../src/main/java/com/thiagsilvadev/erpapi/iam/internal/service/JwtTokenService.java),
[security configuration](../../src/main/java/com/thiagsilvadev/erpapi/iam/internal/config/IamSecurityConfiguration.java),
[tenant authorization](../../src/main/java/com/thiagsilvadev/erpapi/iam/accesscontrol/internal/service/TenantAuthorizationService.java).
