# ADR 0007: API and web application in one monorepo

Status: Accepted (retrospective).

## Context

The ERP API and web application were maintained in separate repositories.
Changes to the API contract and its frontend consumers often need to be
understood and validated together, while each application retains its own
build tools and runtime.

## Decision

Maintain both applications in one Git repository: the Spring Boot API in
`apps/api` and the Angular frontend in `apps/web`. Keep shared documentation
and architecture decisions under `docs` at the repository root. Preserve the
histories of both original repositories in the monorepo.

Each application retains its own dependencies, build and test commands. The
frontend generates its API clients locally from the running API's OpenAPI
document; generated clients are not tracked in Git.

## Consequences

Related API and frontend changes can be reviewed together, and documentation
has a common entry point. Developers must follow the setup sequence to generate
frontend clients before building or testing the web application. A shared
repository does not merge the application runtimes or imply a shared build or
deployment pipeline.

## References

[Repository setup](../../README.md), [API setup](../../apps/api/README.md),
[web setup](../../apps/web/README.md),
[client generation configuration](../../apps/web/orval.config.ts),
