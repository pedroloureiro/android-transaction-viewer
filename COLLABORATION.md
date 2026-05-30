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

### `feat: initialise build configuration and project dependencies`

**What was done:** Added all library dependencies and plugin configuration to `libs.versions.toml`, `build.gradle.kts` (root and app), and `gradle.properties`.

**My role:** Defined the full technology stack upfront during the grill session — Koin, Retrofit + OkHttp + Kotlin Serialization, Room, Paging 3, and their test counterparts. Explicitly asked AI to verify version compatibility rather than accepting its initial estimates.

**Where I challenged AI:**

- **Version accuracy:** AI produced initial version estimates for several libraries without verifying them. I questioned whether they were correct and up to date. AI then ran a proper lookup and found significant errors in its own estimates: KSP was wrong (`1.0.29` does not exist for Kotlin 2.2.10 — the correct version is `2.2.10-2.0.2`), Retrofit had a major version bump to 3.0.0, Room was at 2.8.4 not 2.6.1, and the JakeWharton Retrofit serialization converter was archived in March 2024 and should not be used.

- **Retrofit converter choice:** AI initially reached for the JakeWharton converter out of habit. I had AI verify, and it confirmed the Square-maintained `converter-kotlinx-serialization` at version 3.0.0 is now the correct choice.

**Build issues encountered and resolved:**

1. `kotlin.android` plugin conflict — AGP 9.x registers the Kotlin extension internally; applying `kotlin.android` explicitly caused a "extension already registered" error. Removed.
2. `kotlinOptions` block — only available when `kotlin.android` is explicitly applied. Removed; JVM target is already covered by `compileOptions`.
3. KSP source set conflict — KSP tries to register generated sources via the Kotlin source sets DSL, which AGP 9.x "built-in Kotlin" disallows by default. Fixed with `android.disallowKotlinSourceSets=false` in `gradle.properties`, as directed by AGP's own error output.

**Final state:** Clean `assembleDebug` build, all 37 tasks executed successfully.

### `feat: add domain layer with Transaction model, use case, and repository interface`

**What was done:** Created the full domain layer — `Transaction`, `Amount`, `Initiator`, `BankAccount`, `TransactionSide`, `TransactionStatus` models; `TransactionRepository` interface; `GetTransactionsUseCase`; and `GetTransactionsUseCaseTest`.

**My role:** Defined the scope of the domain layer during the grill session — pure Kotlin, no Android framework dependencies, `TransactionSide` and `TransactionStatus` as typed enums rather than raw strings, full model covering all API fields not just the ones displayed.

**Style decision:** I specified that interfaces and their implementations should live in the same file. `TransactionRepository` holds only the interface for now; its implementation will be added to the same file in Task 6.

**Testing decision — use case test deleted:**
AI initially wrote a `GetTransactionsUseCaseTest` that verified `invoke()` delegates to the repository and returns its flow. I challenged whether this was worth keeping at all. The use case is a single line of delegation — no branching, no transformation, no error handling. The test was asserting that MockK records a method call correctly, not that any business logic behaves correctly.

Decision: delete the test. If `GetTransactionsUseCase` gains real logic in the future, that is the moment to write tests — not preemptively for a delegation that has nothing to fail.

Test effort is concentrated in `TransactionRemoteMediator` where there are real branches, error paths, and state transitions that can go wrong in non-obvious ways. That is a more honest and defensible testing strategy than spreading coverage evenly across all classes regardless of their complexity.

### `feat: add Room database with transaction and remote key entities`

**What was done:** Created `TransactionEntity` with `@Embedded` for `AmountEmbedded`, `InitiatorEmbedded?`, and `BankAccountEmbedded` using explicit column prefixes to avoid naming conflicts. `RemoteKeyEntity` uses a single fixed-ID row to store the session seed and next page. `TransactionDao` and `RemoteKeyDao` cover insert, query, and clear operations. `AppDatabase` wires both entities and DAOs.

**My role:** Flagged that `RemoteKeyDao.get()` may be unused once the full wiring is in place — added a `// TODO: remove if unused` comment rather than deleting it prematurely.

**AI contribution:** Generated all Room boilerplate. Used prefixed `@Embedded` fields for all nested types (`amount_`, `initiator_`, `bank_account_`) to prevent column name collisions with `id` shared across embedded types.

### `refactor: consolidate domain model types into Transaction.kt`

**What was done:** Merged `Amount`, `Initiator`, `BankAccount`, `TransactionSide`, and `TransactionStatus` into `Transaction.kt`. All five types are only referenced by `Transaction` — they have no standalone identity that justifies separate files.

**My role:** Spotted the inconsistency with the DTO file (which already consolidated all related types) and applied the same rule to the domain model.

---

### `feat: add Retrofit service, response DTOs, and domain mappers`

**What was done:** All DTOs consolidated in one file (`TransactionResponseDto.kt`). `TransactionService` defines the Retrofit interface. `TransactionMapper` provides `TransactionDto.toEntity()` and `TransactionEntity.toDomain()` as extension functions.

### `feat: add TransactionRemoteMediator, TransactionLocalDataSource, and tests`

**What was done:** `TransactionLocalDataSource` (interface + impl) introduced as the single gateway between the database layer and all other layers. `TransactionRemoteMediator` refactored to depend only on `TransactionLocalDataSource` and `TransactionService`. `PaginationState` added as a purpose-built type in `data/local/model/` so the mediator never sees `RemoteKeyEntity`. Tests written for both the mediator (8 cases) and the mapper (7 cases).

**My role — key design decisions:**

- **TransactionLocalDataSource as single DB gateway:** I challenged the mediator having direct DAO access — it gave the mediator more operations than it needed and knowledge of the DB schema. I proposed a `TransactionLocalDataSource` facade that exposes only what each layer actually needs.
- **PaginationState over RemoteKeyEntity:** I pushed for the mediator not to receive `RemoteKeyEntity` at all. The local data source maps the entity to a purpose-built `PaginationState(seed, nextPage)` — a network concept, not a DB concept.
- **MAX_PAGE stays in the mediator:** I decided that `MAX_PAGE` is an API contract, not a storage concern. The mediator owns pagination termination logic.
- **Asymmetry accepted:** I considered adding `TransactionRemoteDataSource` to mirror `TransactionLocalDataSource`, but decided it was premature abstraction for one endpoint in a 2-day exercise.
- **`TransactionLocalDataSourceImpl` not unit tested:** Testing its atomicity requires a real Room database. Skipped for now — Robolectric + in-memory Room is a noted polish step if time allows.
- **`runInTransaction` callback removed:** Initially injected as a function for testability. Removed once the mediator was fully decoupled — the impl owns the database and can call `withTransaction` directly.

### `feat: add TransactionRepositoryImpl, Koin modules, and Application class`

**What was done:** `TransactionRepositoryImpl` added to `TransactionRepository.kt` (interface and impl co-located per project convention). Koin wiring split across four modules: `NetworkModule`, `DatabaseModule`, `RepositoryModule`, `UseCaseModule`. `TransactionViewerApplication` bootstraps Koin on startup. `AndroidManifest.xml` registers the Application class and INTERNET permission. `BASE_URL` moved to `gradle.properties` and surfaced via `BuildConfig` — no hardcoded URL in code or build scripts.

**My role — key design decisions:**

- **BASE_URL in `gradle.properties` via `BuildConfig`:** I rejected hardcoding the URL in `build.gradle.kts` or the DI module. `gradle.properties` is the right home for environment config; `BuildConfig` is the compile-time bridge. The setup is one step away from supporting divergent dev/prod URLs via product flavors — without adding that complexity now.
- **DB name as `private const val` in `AppDatabase.companion`:** I challenged the DB name living in the DI module. The name is schema configuration — it belongs next to the schema definition, not in the wiring layer. A companion factory `AppDatabase.create(context)` co-locates the name with the class. Atomicity is unaffected: `withTransaction` governs that, not how the instance is constructed.
- **Separate modules per concern:** I challenged collapsing repository and use case into a single `AppModule`. The existing pattern (separate file per module) should be consistent — `RepositoryModule` and `UseCaseModule` are distinct concerns with distinct future growth paths.

### `feat: add TransactionListViewModel with Paging 3 integration`

**What was done:** `TransactionListViewModel` exposes a single `Flow<PagingData<Transaction>>` via `cachedIn(viewModelScope)`. `ViewModelModule` registers it with Koin. `TransactionViewerApplication` updated to include the new module.

**My role — key design decision:**

- **Dropped `TransactionUiState`:** The ROADMAP called for a separate `StateFlow<TransactionUiState>`. I challenged whether it was needed given Paging 3's built-in load state. `LazyPagingItems.loadState` already exposes loading, error, and end-of-pagination states — all derived from the same paging stream the ViewModel owns. A parallel `StateFlow` would duplicate that and risk the two streams drifting out of sync, which would actually break single source of truth rather than enforce it. The ViewModel is still the single source of truth: the screen derives all state — items and load state — from the one flow it exposes.

### `feat: add TransactionListScreen with Compose UI`

**What was done:** `TransactionListScreen` with `collectAsLazyPagingItems()`, full load state handling (refresh loading/error, append loading/error, retry). `AppListItem` generic component in `ui/components/` — takes strings and colors, knows nothing about the domain. `TransactionItem` private mapper in the screen file translates `Transaction` → `AppListItem`. `AmountFormatter` and `DateFormatter` utilities in `ui/utils/` as classes with companion objects. `MainActivity` wired to the screen with a `TopAppBar`. All hardcoded strings moved to `strings.xml`. Credit colour named in `Color.kt`.

**My role — key design decisions:**

- **Generic `AppListItem` in `ui/components/`:** I proposed a reusable component that accepts plain strings and colours rather than a domain-specific `TransactionItem`. The domain mapping stays in a private function in the screen file, co-located with its only consumer. Future list screens can reuse `AppListItem` with different configs.
- **No hardcoded strings or colours in code:** Established as a general rule — all string literals go to `strings.xml` via `stringResource`, all colour literals go to `Color.kt` with named constants.
- **`DateFormatter` and `AmountFormatter` as classes with companion objects:** Format strings and timezone identifier extracted as `private const val`. Output locale uses `Locale.getDefault()` for localised month names; input parsing uses `Locale.US` for stable ISO 8601 parsing. `formatSettledAt` returns nullable String — null handled at the composable call site with `stringResource`, not inside the formatter.
- **`MainActivity` stays at root:** Challenged whether it should move to `ui/`. Decision: `MainActivity` is an Android system entry point, not a UI component — it interacts with the OS in ways that go beyond the `ui/` package contract.

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
