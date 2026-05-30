# ADR 0004: Treat API Seed as Permanent Session Identifier

**Status:** Accepted  
**Date:** 2026-05-30

## Context

The Transactions API generates deterministic data per seed — same seed + same page always returns the same transactions. The seed can be omitted on the first request, after which the API returns the seed it used in `info.seed`.

## Decision

The seed captured from the first-ever API response is **persisted in Room and never changed** for the lifetime of the app install. Every subsequent API call — including those triggered by pull-to-refresh, process death, or background/foreground transitions — uses this same seed.

The seed resets only on app reinstall or explicit storage/cache clearance.

## Rationale

- The seed functions as a stable account/session identifier: the user always sees *their* transactions, not a new random set on every refresh.
- This mirrors how a real banking app behaves — refreshing shows updated data for the same account, not a completely different account.
- Pull-to-refresh re-fetches from page 1 with the same seed, consistent with "check for new transactions on this account" semantics.

## Alternatives Considered

- **Reset seed on REFRESH**: each pull-to-refresh or app restart would produce a completely different transaction list — inconsistent with the session-token mental model and confusing to users.
- **Reset button / logout**: adds scope (navigation, auth state) with no requirement from the exercise. Parked as out of scope.

## Consequences

- In this mock API, pull-to-refresh is functionally a no-op on the data (same seed + same pages = identical transactions). This is an acknowledged limitation of the mock API, not a design flaw — a real API would return genuinely new transactions for the same account.
- There is no in-app mechanism to reset the seed. This is intentional.
