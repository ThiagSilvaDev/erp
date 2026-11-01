# ADR 0006: Authorization constants and after-commit side effects

Status: Accepted.

## Context

Authorization expressions and after-commit side effects were implemented in
several competing styles, which made review harder and hid two real risks:

- Around sixty use cases guarded methods with `hasAuthority(T(com.example.Permissions).READ)`.
  The fully qualified name lives inside a string: it is not found by IDE
  refactorings, it can reference any class regardless of module boundaries, and a
  stale type or constant name fails only at runtime.
- Four components registered `TransactionSynchronization` by hand to publish events
  or provision credentials after commit. Copying that logic invites a variant that
  publishes before commit or never runs outside a transaction.
- The race path of the nine create use cases catches `DataIntegrityViolationException`.
  That catch is only correct while it immediately rethrows: swallowing the violation
  leaves the transaction rollback-only and the commit fails with
  `UnexpectedRollbackException`.

## Decision

1. `@PreAuthorize` expressions are built from static-imported permission constants,
   for example `hasAuthority('" + WRITE + "')`. SpEL type references (`T(...)`) are
   not used. javac folds the constant into the annotation value, so the expression
   contains no type reference, no parsing work and no fully qualified name.
2. After-commit side effects go through
   `common/transaction/AfterCommit.run(...)`, which registers a synchronization when
   one is active and executes immediately otherwise.
3. Raw passwords and other secrets never travel through the application event bus.
   Credential provisioning stays a direct call; the Modulith event registry can
   persist events that have transactional listeners.
4. Unique-constraint races keep the `existsBy...` pre-check plus a
   `DataIntegrityViolationException` catch that always rethrows a domain exception.

`ArchitectureRulesTest` and `SourceConventionsTest` enforce these decisions.

## Consequences

- Permission names participate in normal compilation and renaming. Changing a
  constant value recompiles its dependents instead of silently leaving stale SpEL.
- A cross-module permission that cannot be imported without creating a module cycle
  (IAM assigns roles, IAM already depends on `user::api`) is declared as a local
  constant with a comment naming the avoided cycle.
- After-commit behavior is defined once, so event publication and credential
  provisioning share the same transaction semantics.
- The conflict-translation tests must use real PostgreSQL with explicit transaction
  boundaries; a mocked repository cannot reveal the rollback-only failure mode.

## References

[Request, persistence and transaction flow](../ARCHITECTURE.md#request-persistence-and-transaction-flow),
[AfterCommit](../../src/main/java/com/thiagsilvadev/erpapi/common/transaction/AfterCommit.java),
[UserEmailUniquenessPersistenceTest](../../src/test/java/com/thiagsilvadev/erpapi/user/internal/repository/UserEmailUniquenessPersistenceTest.java),
[SourceConventionsTest](../../src/test/java/com/thiagsilvadev/erpapi/SourceConventionsTest.java),
[ADR 0004](0004-module-integration.md).
