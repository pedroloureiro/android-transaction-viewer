# ADR 0003: Use MVVM (not MVI) for UI Pattern

**Status:** Accepted  
**Date:** 2026-05-30

## Context

The app has a single transaction list screen backed by Paging 3. Two patterns were considered: MVVM and MVI.

## Decision

Use **MVVM** with a structured `StateFlow<TransactionUiState>` and a separate `Flow<PagingData<Transaction>>`.

## Rationale

- The screen state is simple: loading / success / error — no multi-step state machines or complex side-effect chains that MVI earns its keep on.
- Paging 3 already imposes unidirectional data flow for the list stream (`PagingSource → RemoteMediator → PagingData → LazyPagingItems`). Adding MVI on top creates a hybrid: `PagingData` doesn't fit cleanly into a single `UiState` sealed class, undermining MVI's "single state source" premise.
- Direct ViewModel function calls (`refresh()`, `retry()`) are sufficient — no sealed `Intent` class needed.

## Alternatives Considered

**MVI** — explicit `Intent` sealed class as input, single `UiState` as output, `SideEffect` channel for one-shot events. Valuable for complex flows with many state transitions. For this screen, the ceremony outweighs the benefit.

## Consequences

- If the app grows significantly in complexity (multi-step flows, optimistic updates, undo/redo), individual screens can migrate to MVI independently without affecting this screen.
- MVVM + Paging 3 is the pattern documented in the official Android developer guides — reviewers will find it immediately familiar.
