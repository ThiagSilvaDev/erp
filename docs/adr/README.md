# Architecture decision records

[Architecture](../ARCHITECTURE.md) · [Domain](../DOMAIN.md) ·
[Security](../SECURITY.md) · [Testing](../TESTING.md)

ADRs capture a consequential architectural choice, the context that makes it
relevant and its observable consequences. The initial records are retrospective:
they describe the current implementation and repository policy, not a recovered
history of meetings, adoption dates or rejected alternatives.

| ADR | Decision |
| --- | --- |
| [0001](0001-modular-monolith.md) | Modular monolith and public module contracts |
| [0002](0002-domain-and-use-cases.md) | Domain behavior, use cases and ID references |
| [0003](0003-token-contexts.md) | Stateless JWTs with tenant and platform contexts |
| [0004](0004-module-integration.md) | Synchronous stock coordination and persisted events |
| [0005](0005-development-schema.md) | Development schema updates and deferred Flyway adoption |

## Adding a decision

Use the next four-digit number and a descriptive filename. Keep the record brief
and link to evidence in code or other documentation. Distinguish proposed choices
from implemented ones. A changed decision should link to the record it supersedes;
do not silently rewrite an old decision as if it had always been the new one.
Include alternatives only when their consideration is known.

```markdown
# ADR NNNN: Decision title

Status: Proposed / Accepted / Superseded by ADR NNNN.

## Context
What concrete constraint or problem motivates this decision?

## Decision
What choice is being made, and where does it apply?

## Consequences
What does the choice enable, constrain or require operationally?

## References
Links to implementation, related documents and superseded decisions.
```
