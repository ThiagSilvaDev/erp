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
- Bean Validation
- MapStruct
- Springdoc OpenAPI
- JUnit 5, AssertJ, Mockito, and Spring Modulith Test
- Maven Wrapper and Spotless

## Architecture

The code is organized by business capability under
`src/main/java/com/thiagsilvadev/erpapi`:

| Module | Responsibility |
| --- | --- |
| `catalog` | Products and categories |
| `company` | Companies, addresses, and company contacts |
| `customer` | Customers, suppliers, and contact people |
| `financial` | Accounts, ledger entries, invoices, and payments |
| `iam` | Authentication, JWT issuance, and authorization |
| `inventory` | Warehouses, stock, movements, and reservations |
| `logistics` | Drivers, vehicles, shipments, and tracking |
| `sales` | Sales orders and their lifecycle |
| `user` | Users and company memberships |

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

Run the application with the development profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The API starts at `http://localhost:8080`. The development profile creates the
schema through Hibernate and loads deterministic sample data.

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

## API documentation

With the application running, the OpenAPI document is available at:

```text
http://localhost:8080/v3/api-docs
```

The API is rooted at `/api/v1`. Endpoint groups include authentication,
catalog, companies, customers, suppliers, users, inventory, sales, finance,
and logistics.

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
