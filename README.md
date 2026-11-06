# ERP monorepo

This repository contains the Spring Boot API in `apps/api` and the Angular application in `apps/web`.

## Local development

Requirements: Java 21, Docker Compose, Node.js and npm (see the frontend package manifest for its npm version).

1. Start PostgreSQL: `cd apps/api && docker compose up -d postgres`.
2. In `apps/api`, start the API: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`. See the API README for development credentials and configuration.
3. In another terminal, run `cd apps/web && npm ci`.
4. Once `http://localhost:8080/v3/api-docs` is available, run `npm run api:generate` in `apps/web` to create the ignored TypeScript API clients. Repeat when the API contract changes.
5. Run `npm start` in `apps/web` and open http://localhost:4200.

## Validation

Run `./mvnw verify` in `apps/api` with PostgreSQL available. After generating the API clients, run `npm run build` and `npm test -- --watch=false` in `apps/web`.

Documentation: [architecture](docs/ARCHITECTURE.md), [domain](docs/DOMAIN.md), [glossary](docs/GLOSSARY.md), [security](docs/SECURITY.md), [testing](docs/TESTING.md), and [decisions](docs/adr/README.md).
