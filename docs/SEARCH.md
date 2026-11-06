# Search and list query conventions

Search criteria are module-internal request DTOs, bound as optional query parameters on list endpoints. `KpiCriteria` and `AccountBalanceCriteria` represent report inputs and do not need the `SearchCriteria` suffix.

## Matching behavior

- Name fields use case-insensitive contains matching. Leading and trailing whitespace is ignored.
- `%`, `_` and `\\` are treated as literal characters in name searches rather than SQL `LIKE` wildcards.
- Product SKU, category code, email and customer/supplier documents use normalized exact matching.
- Company CNPJ uses a digits-only prefix match. If a nonblank CNPJ filter contains no digits, it matches no companies rather than silently disabling the filter.
- Blank text filters are ignored. Enum and UUID filters are exact.
- Criteria fields that include company/tenant IDs are additional filters only. Query use cases must always apply the authenticated tenant scope independently; clients cannot use these values to broaden visibility.

## Date filters

Accounting entry date filters are optional as a pair: omit both `from` and `to`, or supply both. A supplied range must be ordered and its endpoints are inclusive. Account balance dates are required and ordered. KPI dates remain subject to KPI-specific range validation.

## Pagination

List endpoints use Spring Data `Pageable`. Client-controlled page size is capped at 200 by `spring.data.web.pageable.max-page-size`. APIs should add a stable default sort when result order is part of their contract. Arbitrary sort properties are not an explicit search filter; invalid fields are handled by the existing request-error mapping.

## Validation and tests

Criteria exposed at HTTP boundaries should use `@Valid` and Bean Validation constraints for bounded text and field-specific rules. Search tests should cover omitted and combined filters, normalization, wildcard literals, invalid criteria, pagination and tenant isolation. Use PostgreSQL-backed tests when validating actual JPA predicate behavior; mocked repository tests alone do not prove the generated query semantics.
