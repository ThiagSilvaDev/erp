# ERP API

REST API for an ERP system built as a modular monolith with Spring Boot and
Spring Modulith. The application covers catalog, companies, customers, finance,
identity and access management, inventory, logistics, sales, and users.

## Technology stack

- Java 21
- Spring Boot 4
- Spring Modulith
- Spring Data JPA and PostgreSQL
- Spring Security with JWT bearer tokens
- Micrometer and OpenTelemetry
- Grafana LGTM (Loki, Grafana, Tempo, and Mimir)
- Bean Validation
- MapStruct
- Springdoc OpenAPI
- JUnit 5, AssertJ, Mockito, and Spring Modulith Test
- Maven Wrapper and Spotless

## Technical documentation

- [Architecture](docs/ARCHITECTURE.md): structure, modules, dependencies and workflows.
- [Glossary](docs/GLOSSARY.md): canonical ERP terms and module ownership.
- [Domain](docs/DOMAIN.md): ERP concepts and implemented business rules.
- [Security](docs/SECURITY.md): authentication, authorization and token lifecycle.
- [Testing](docs/TESTING.md): strategy, conventions and database prerequisites.
- [Architecture decisions](docs/adr/README.md): current choices and their consequences.
- [Agent instructions](AGENTS.md): repository working rules and commit conventions.

## Architecture

The code is organized by business capability under
`src/main/java/com/thiagsilvadev/erpapi`:

| Module | Responsibility |
| --- | --- |
| `catalog` | Products and categories |
| `company` | Companies, addresses, and company contacts |
| `customer` | Customers, suppliers, and contact people |
| `financial` | Financial obligations, cash movements, accounting, invoices, and payments |
| `iam` | Authentication, credentials, login, and JWT issuance |
| `iam.accesscontrol` | Tenant memberships, roles, permissions, platform assignments, and authorization audit |
| `inventory` | Warehouses, stock, movements, and reservations |
| `kpi` | Tenant-scoped executive business indicators |
| `logistics` | Drivers, vehicles, shipments, and tracking |
| `sales` | Sales orders and their lifecycle |
| `user` | Global user identities and profiles |

Each business module separates its public contracts from its implementation:

```text
module/
├── api/       # Facades, commands, snapshots, events, and shared enums
└── internal/  # Controllers, use cases, domain models, repositories, and DTOs
```

Cross-module calls must go through the target module's `api` package or through
application events. Module boundaries are verified by
`ApplicationModularityTests`.

## Prerequisites

- JDK 21
- Docker with Docker Compose

The Maven Wrapper is included, so a separate Maven installation is not needed.

## Running locally

Start PostgreSQL:

```bash
docker compose up -d
```

Optionally start the local observability stack (Grafana LGTM) as well:

```bash
docker compose --profile lgtm up -d
```

Run the application with the development profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

With LGTM running, also activate the `otel` profile so the app exports telemetry:

```bash
docker compose --profile lgtm up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,otel
```

The API starts at `http://localhost:8080`. The development profile creates the
schema through Hibernate and loads deterministic sample data. OTLP export is
disabled unless the `otel` profile is active (avoids connection errors when
LGTM is not running).

Development-only administrator credentials:

```text
Email:    admin@acme.example
Password: ChangeMe123!
```

These credentials and the default development JWT secret must never be used in
production.

## Authentication

Authentication uses stateless JWT bearer tokens. The login endpoint returns a
global token and the companies available to the user:

```bash
curl --request POST http://localhost:8080/api/v1/auth/login \
  --header 'Content-Type: application/json' \
  --data '{
    "email": "admin@acme.example",
    "password": "ChangeMe123!"
  }'
```

When a user belongs to more than one company, select a company with the global
token and one of the `companyId` values returned at login:

```bash
curl --request POST http://localhost:8080/api/v1/auth/select-company \
  --header 'Authorization: Bearer <global-token>' \
  --header 'Content-Type: application/json' \
  --data '{
    "companyId": "<company-id>"
  }'
```

Use the scoped token returned by that request for business endpoints:

```bash
curl http://localhost:8080/api/v1/catalog/products \
  --header 'Authorization: Bearer <scoped-token>'
```

If the user has only one active company membership, the login response also
contains a scoped token in `scopedToken`.

A membership can have multiple roles. Scoped tokens expose the selected
membership, its authorization version, assigned role codes, and the effective
union of their permissions. The API validates that version against the active
membership on every scoped request, so role changes and membership deactivation
invalidate existing scoped tokens immediately.

Tenant role and membership administration is available under:

```text
/api/v1/iam/roles
/api/v1/iam/memberships
```

Every company receives the built-in `OWNER`, `ADMIN`, `MANAGER`, `OPERATOR`, and
`DRIVER` roles. Custom roles are tenant-owned, and assigned roles must belong to
the active tenant. The authorization model stores an explicit assignment scope;
the current API grants tenant-wide assignments and reserves branch and warehouse
scope types for future scoped workflows.

Platform administration is separate from tenant roles. A platform administrator
can exchange the global token at `POST /api/v1/auth/select-platform` and use the
returned token for endpoints under `/api/v1/platform`, including company
administration at `/api/v1/platform/companies`.

## API documentation

With the application running, the OpenAPI document is available at:

```text
http://localhost:8080/v3/api-docs
```

The API is rooted at `/api/v1`. Endpoint groups include authentication,
catalog, companies, company contacts, customers, suppliers, contact persons,
users, inventory, sales, finance, logistics, and executive KPIs.

### Executive KPIs

The tenant-scoped executive dashboard is available to administrators and
managers:

```bash
curl 'http://localhost:8080/api/v1/kpis/executive?from=2026-07-01&to=2026-07-31&bucket=WEEK' \
  --header 'Authorization: Bearer <scoped-token>'
```

`from` and `to` are inclusive UTC calendar dates and must either both be
provided or both omitted. When omitted, the endpoint returns the latest 30
days. `bucket` accepts `DAY`, `WEEK`, or `MONTH` and defaults to `DAY`; ranges
are limited to 366 days. Monetary totals are returned separately by currency,
and period-backed metrics include comparison with the immediately preceding
equal-length period. Each business section exposes its summary under `metrics`
and its time series under `trend`; inventory and logistics also include a
point-in-time `current` snapshot.

## Observability

OTLP export is opt-in via the Spring `otel` profile and targets the Grafana
LGTM service in `compose.yaml`. LGTM itself is opt-in via the Compose `lgtm`
profile (`docker compose --profile lgtm up -d`); plain `docker compose up -d`
starts only PostgreSQL. Use both together (`dev,otel` + `--profile lgtm`).

| Service | URL | Credentials |
| --- | --- | --- |
| Grafana | `http://localhost:3000` | `admin` / `admin` |
| OTLP HTTP | `http://localhost:4318` | None |
| OTLP gRPC | `http://localhost:4317` | None |
| Application health | `http://localhost:8080/actuator/health` | None |

Grafana opens the provisioned **ERP API Observability** dashboard by default.
It summarizes request rate, server errors, p95 latency, and process CPU usage.
HTTP charts break down traffic by endpoint, method, and status, with p95 and p99
latency. Endpoint and HTTP method filters apply to HTTP panels only; JVM and
database panels show heap usage, CPU history, average GC pause duration, and
Hikari connection pool usage, waiting threads, and timeouts. Missing telemetry
is shown as **No data**. Metrics are published every ten seconds, so new
applications or requests may take a short time to appear.

Application logs are available through the Loki data source and request traces
through Tempo. Spring Boot adds `traceId` and `spanId` correlation values to
request logs, allowing a log entry to be matched to its trace without a custom
request ID.

The development endpoints can be overridden independently:

```bash
export OTEL_EXPORTER_OTLP_ENDPOINT='http://localhost:4318'
# Or configure signal-specific endpoints:
export OTEL_EXPORTER_OTLP_LOGS_ENDPOINT='http://localhost:4318/v1/logs'
export OTEL_EXPORTER_OTLP_METRICS_ENDPOINT='http://localhost:4318/v1/metrics'
export OTEL_EXPORTER_OTLP_TRACES_ENDPOINT='http://localhost:4318/v1/traces'
```

OTLP export is disabled unless the `otel` profile is active (or equivalent
flags are set). A deployed environment must explicitly enable OpenTelemetry and
OTLP metrics export and provide collector endpoints:

```bash
export MANAGEMENT_LOGGING_EXPORT_OTLP_ENABLED=true
export MANAGEMENT_TRACING_EXPORT_OTLP_ENABLED=true
export MANAGEMENT_OTLP_METRICS_EXPORT_ENABLED=true
export OTEL_EXPORTER_OTLP_ENDPOINT='https://collector.example'
```

The bundled LGTM container is intended only for local development and testing;
it is not a production observability deployment.

## Configuration

The development database defaults match `compose.yaml`:

| Setting | Development value |
| --- | --- |
| Database | `mydatabase` |
| Username | `myuser` |
| Password | `secret` |
| Port | `5432` |

Outside the development profile, provide a strong JWT signing secret:

```bash
export ERP_IAM_JWT_SECRET='<strong-secret-at-least-32-bytes>'
```

Database connection and production schema-management settings should also be
provided by the deployment environment. The repository currently has no
database migration tool configured; the development-only Hibernate
`ddl-auto=update` setting must not be relied on in production.

## Build and test

Run the complete verification build:

```bash
./mvnw verify
```

Other useful commands:

```bash
./mvnw test
./mvnw spotless:check
./mvnw spotless:apply
```

Tests live under `src/test/java/com/thiagsilvadev/erpapi` and mirror the
production module structure.

## Development guidelines

- Keep changes within the module that owns the behavior.
- Expose cross-module contracts only through `api`.
- Keep controllers thin and transactions in use cases.
- Put business invariants in domain entities and value objects.
- Keep repositories focused on persistence.
- Add or update tests whenever behavior changes.
- Run `./mvnw verify` before submitting a change.
