# ADR 0005: Detect End of Pagination via API Contract Max Page

**Status:** Accepted  
**Date:** 2026-05-30

## Context

The Transactions API explicitly documents that the `page` parameter is clamped to [1, 10000]. There is no `hasMore` flag in the response.

## Decision

In the `RemoteMediator`, signal end of pagination when `requestedPage > MAX_PAGE`, where `MAX_PAGE = 10000` matches the API contract.

```kotlin
const val MAX_PAGE = 10_000

endOfPaginationReached = requestedPage > MAX_PAGE
```

## Rationale

- Follows the documented API contract rather than relying on observed/inferred behavior.
- Explicit and self-documenting — a reader can verify the constant against the API spec.
- No extra DB queries, no response field comparison.

## Alternatives Considered

- **`info.page` mismatch detection**: elegant but couples termination logic to undocumented response behavior rather than the contract. Rejected in favour of the explicit contract.
- **ID comparison**: adds a DB query per page load with no benefit over the simpler cap.

## Consequences

- If the API raises its page cap, `MAX_PAGE` must be updated to match.
- Pages beyond 10,000 are unreachable by design — consistent with the API contract.
