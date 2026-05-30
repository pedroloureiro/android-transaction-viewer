# AI Collaboration Log

This document is a transparent record of how AI (Claude) was used during this skills test exercise. It is updated before every commit and will be used as the basis for the Part 2 presentation.

---

## Context

Skills test for a Senior Android Engineer position at Qonto.  
Time constraint: 2 days.  
AI tool used: Claude (Anthropic) via Claude Code CLI.

---

## How AI Was Used

### What I used AI for
- Architecture design sessions — AI grilled me on decisions before any code was written
- Generating ADRs to document hard decisions we reached together
- Producing the implementation roadmap based on agreed decisions
- Code generation for boilerplate-heavy layers (Room entities, Koin modules, Retrofit setup)
- Writing and reviewing unit tests

### What I chose not to use AI for
- The architectural intent and product thinking behind this app — those came from me
- Final calls on trade-offs — I made the decisions, AI stress-tested them

### Where I drove the decisions

**DI framework — Koin over Hilt:**  
AI recommended Hilt as the safer, more familiar interview choice. I independently researched Koin, concluded it was appropriate for a small-to-medium app, and overrode the recommendation. The simpler setup and Kotlin-native DSL were the right call for this scope.

**HTTP + serialization stack:**  
I brought the full stack — Retrofit + OkHttp + Kotlin Serialization — to the table myself. AI confirmed the choice was consistent and well-reasoned.

**MVVM vs MVI:**  
AI moved to MVVM quickly. I pushed back and challenged whether MVI would be more scalable as the app grew. We worked through the trade-offs in depth — including whether migration paths between the two are realistic in practice. I concluded MVVM was the right fit here, specifically because Paging 3 already imposes unidirectional data flow on the list stream, which removes MVI's main justification for this screen.

**Seed as a permanent session identifier:**  
This was entirely my framing. The API returns a seed in every response; AI initially suggested resetting it on pull-to-refresh to simulate "fresh data." I reframed the seed as an account-scoped session token — the user should always see the same transactions, mirroring how a real banking app behaves. I held this position through follow-up grilling and it shaped the entire `RemoteMediator` design.

**Pagination termination — following the API contract:**  
AI proposed a clever approach: detect the end of pagination by comparing the requested page against the `info.page` value in the response (which the API clamps). I verified this empirically, then pushed back — the API explicitly documents a 10,000 page cap, so we should honour the contract rather than rely on observed clamping behaviour. Following the spec is more defensible and more robust.

**No reset mechanism:**  
AI explored several options for a session reset (hidden long-press, logout screen, pull-to-refresh override). I evaluated each and ruled them all out — no requirement in the exercise, and each option added scope or muddied an already deliberate design decision. The right answer was no reset mechanism.

---

## Interaction Log

### Session 1 — Architecture Design (Grill Session)
**Date:** 2026-05-30  
**Format:** Question-by-question grilling across 11 decisions before writing a single line of code.

| # | Decision | Outcome | Who drove it |
|---|---|---|---|
| 1 | DI framework | Koin | Me — overrode AI's Hilt recommendation |
| 2 | HTTP + serialization | Retrofit + OkHttp + Kotlin Serialization | Me — proposed the full stack |
| 3 | UI pattern | MVVM + structured `UiState` | Me — challenged AI on MVI, reached MVVM through reasoned trade-off |
| 4 | Module structure | Single module, layer-first packages | Me — quick, deliberate call |
| 5 | API pagination mechanism | Page-number + seed | Me — provided API docs and sample payload to inform the design |
| 6 | Seed strategy | Permanent session identifier, persisted in Room | Me — introduced the session-token framing |
| 7 | REFRESH behaviour | Re-fetch page 1 with same seed (no reset) | Me — held position against AI's initial suggestion |
| 8 | Pagination termination | `MAX_PAGE = 10_000` per API contract | Me — overrode AI's empirical approach in favour of the spec |
| 9 | Room nested objects | `@Embedded` | Joint — quick agreement |
| 10 | Testing scope | `RemoteMediator` + `UseCase` only | Joint — agreed on highest-value targets |
| 11 | Page size + loading/error UI | 20/page, spinner + inline retry | AI defaults — accepted as reasonable |

**Outputs from this session:**
- `ROADMAP.md` — 9-task implementation plan with commit messages and bodies
- `docs/adr/0001` through `docs/adr/0005` — Architecture Decision Records
- `COLLABORATION.md` (this file)

---

## Commit Log

*(Updated before each commit)*

| Commit | Description | AI contribution |
|---|---|---|
| feat: initialise build configuration and project dependencies | All library versions, plugin setup, gradle.properties fix for KSP + AGP 9.x compatibility | Version lookup (with correction — initial estimates were wrong on KSP, Retrofit, Room, OkHttp, Koin, Lifecycle); build error diagnosis |

---

## Part 2 — Presentation Answers

*(To be filled in as implementation progresses)*

### Context at the time of the skills test
> To be written.

### Architecture — main components and how they interact
> To be written. Will include a diagram.

### Good practices applied
> To be written. Will reference SOLID, KISS, DRY as observed in the implementation.

### Development strategy
> To be written. Will reference the roadmap, commit strategy, and layer prioritisation.

### Is the code future-proof?
> To be written. Will reference ADRs, clean architecture boundaries, and scalability considerations.
