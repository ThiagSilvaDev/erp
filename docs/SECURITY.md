# Authentication and authorization

[Architecture](ARCHITECTURE.md) · [Domain](DOMAIN.md) ·
[Testing](TESTING.md) · [Decisions](adr/README.md)

## Login and token contexts

Passwords are stored as BCrypt hashes. `POST /api/v1/auth/login` authenticates
credentials and requires at least one active company membership. It returns a
global JWT and available companies; exactly one active membership also produces
a scoped token in the login response.

| Token use | Establishes | Used for |
| --- | --- | --- |
| `global` | User identity without an active tenant | Listing companies and selecting tenant/platform context |
| `scoped` | User, tenant, membership and authorization version | Business endpoints under `/api/v1/**` |
| `platform` | User, platform assignment and authorization version | Administration under `/api/v1/platform/**` |

`POST /api/v1/auth/select-company` accepts a global token and verifies active
membership before issuing a scoped token. `GET /api/v1/auth/companies` lists
available companies using a global token. `POST /api/v1/auth/select-platform`
requires a global token and an active platform assignment. Platform access is
separate from tenant roles; it does not implicitly grant tenant permissions.

The URL filter requires global tokens for context selection and company listing;
use cases retain their own token checks. Health endpoints and, when enabled in
the `dev` profile, the OpenAPI document (`/v3/api-docs`) and Swagger UI
(`/swagger-ui.html`) are publicly accessible (both are disabled by default
outside development). Protected workflows additionally use method-level
permission checks.

Sources: [login](../apps/api/src/main/java/com/thiagsilvadev/erpapi/iam/internal/usecase/LoginUseCase.java),
[company selection](../apps/api/src/main/java/com/thiagsilvadev/erpapi/iam/internal/usecase/SelectCompanyUseCase.java),
[platform selection](../apps/api/src/main/java/com/thiagsilvadev/erpapi/iam/internal/usecase/SelectPlatformUseCase.java),
[security configuration](../apps/api/src/main/java/com/thiagsilvadev/erpapi/iam/internal/config/IamSecurityConfiguration.java).

## Roles, permissions and tenant isolation

A membership connects a global user to a company and can hold multiple roles.
Effective permissions are the union of active assigned roles belonging to that
company. Built-in tenant roles are `OWNER`, `ADMIN`, `MANAGER`, `OPERATOR` and
`DRIVER`; custom roles are tenant-owned. Assignment storage supports tenant,
branch and warehouse scope types, but current authorization resolves tenant
assignments. Branch/warehouse permissions are not implemented as distinct access
workflows yet.

On scoped requests, IAM validates membership ID, user, tenant, active status and
`authz_version` against persisted membership data. Authorities are resolved from
current authorization data rather than trusted solely from JWT claims. Role
updates and membership changes advance the version; old scoped tokens are
rejected and the client must select the company again. Platform requests perform
an equivalent check against the platform assignment and its version.

The tenant filter sets request context from the token and clears it in a
`finally` block. Business use cases use that context and tenant-qualified
repository/API lookups. This is application-level isolation; it should not be
interpreted as a PostgreSQL row-level security policy.

Authorization mutations produce audit records. Audit persistence and failure
behavior are covered by dedicated integration tests, described in
[TESTING.md](TESTING.md).

Sources: [tenant authorization](../apps/api/src/main/java/com/thiagsilvadev/erpapi/iam/accesscontrol/internal/service/TenantAuthorizationService.java),
[platform authorization](../apps/api/src/main/java/com/thiagsilvadev/erpapi/iam/accesscontrol/internal/service/PlatformAuthorizationService.java),
[role updates](../apps/api/src/main/java/com/thiagsilvadev/erpapi/iam/accesscontrol/internal/usecase/UpdateTenantRoleUseCase.java),
[tenant filter](../apps/api/src/main/java/com/thiagsilvadev/erpapi/iam/internal/security/TenantContextFilter.java).

## Session lifecycle and current limits

The API uses bearer tokens and stateless HTTP security; it creates no server-side
HTTP session for login. Tokens are signed with HS256 and expire after the
configured duration, currently one hour. The issuer claim defaults to `erp-api`.
The decoder validates HS256, the configured issuer, required claims and token context.
Signature, timestamp, claim, credential and authorization-version failures return 401.
A valid token with insufficient permissions or the wrong context returns 403.

There is no refresh-token flow, logout API or per-token denylist. Clients obtain
new tokens through login/context selection and can discard local tokens when
signing out. Discarding a token does not invalidate another copy. Membership or
platform assignment deactivation/version changes reject affected scoped or
platform tokens, but do not constitute revocation of the user's global token.
All token contexts revalidate credential status on every authenticated request.
Inactive or missing credentials are rejected. Reactivating a credential permits
reuse of tokens that remain otherwise valid.

Sources: [token issuance](../apps/api/src/main/java/com/thiagsilvadev/erpapi/iam/internal/service/JwtTokenService.java),
[authentication routes](../apps/api/src/main/java/com/thiagsilvadev/erpapi/iam/internal/controller/AuthController.java),
[JWT defaults](../apps/api/src/main/java/com/thiagsilvadev/erpapi/iam/internal/config/IamJwtProperties.java).

## Configuration and handling

Supply `ERP_IAM_JWT_SECRET` outside development. Startup requires at least 32
UTF-8 bytes; production must provide a strong secret externally. The default
`dev` secret and seeded administrator credentials in the [README](../apps/api/README.md)
are for local use only. Changing a shared signing key invalidates tokens signed
with the old key; no overlapping-key rotation mechanism is configured.

Send bearer tokens over HTTPS in deployed environments and do not log passwords,
tokens or signing secrets. Form login, HTTP Basic and CSRF protection are disabled
in the current bearer-token configuration. Browser cookie authentication would
require reconsidering those settings. Client token storage, TLS termination and
production secret distribution are deployment/client concerns, not features
provided by this repository.

[ADR 0003](adr/0003-token-contexts.md) records the current authentication and
authorization choice.
