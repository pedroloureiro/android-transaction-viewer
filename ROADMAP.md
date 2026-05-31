# Implementation Roadmap

## Architecture Decisions Summary

| Decision | Planned | Actual | ADR |
|---|---|---|---|
| DI framework | Koin | Koin | [ADR-0001](docs/adr/0001-dependency-injection-koin.md) |
| Networking | Retrofit + OkHttp + Kotlin Serialization | Retrofit + OkHttp + Kotlin Serialization | [ADR-0002](docs/adr/0002-networking-retrofit-okhttp-kotlin-serialization.md) |
| UI pattern | MVVM + structured `UiState` | MVVM — `UiState` dropped; Paging 3 load state used directly | [ADR-0003](docs/adr/0003-ui-pattern-mvvm.md) |
| Session seed | Persistent, never reset | Renewed on every REFRESH (startup + PTR) | ~~[ADR-0004](docs/adr/0004-seed-as-permanent-session-identifier.md)~~ — superseded |
| Pagination end | `MAX_PAGE = 10_000` per API contract | `MAX_PAGE = 10_000` per API contract | [ADR-0005](docs/adr/0005-pagination-termination-max-page-contract.md) |
| Module structure | Single module, layer-first packages | Single module, layer-first packages | — |
| Room nested objects | `@Embedded` | `@Embedded` | — |
| Page size | 20 results per request | 20 results per request | — |
| Error handling | Not planned | `safeApiCall` + `ApiError` sealed class | — |
| Data sources | `TransactionLocalDataSource` + `TransactionRemoteDataSource` | Removed — repo accesses DB directly | — |
| Test scope | `RemoteMediator` + `UseCase` | `SafeApiCall` + `RepositoryImpl` + `RemoteMediator` + Mapper | — |

## Package Structure (Actual)

```
com.qonto.transactionviewer
├── data/
│   ├── local/
│   │   ├── dao/
│   │   ├── entity/
│   │   └── database/
│   ├── remote/
│   │   ├── dto/
│   │   ├── error/          ← ApiError sealed class
│   │   ├── mapper/
│   │   └── service/
│   └── mediator/           ← callback dispatcher only
├── domain/
│   ├── model/
│   ├── repository/         ← interface + impl co-located
│   ├── usecase/
│   └── util/               ← safeApiCall
└── ui/
    ├── components/
    ├── screen/
    ├── utils/              ← formatters, error extensions
    └── viewmodel/
```

---

## Tasks

### Task 1 — Project dependencies and build configuration ✅
**Commit:** `feat: initialise build configuration and project dependencies`

- Koin, Retrofit + OkHttp + Kotlin Serialization, Room + KSP, Paging 3, test dependencies

---

### Task 2 — Domain layer ✅
**Commit:** `feat: add domain layer with Transaction model, use case, and repository interface`

- `Transaction`, `Amount`, `Initiator`, `BankAccount`, `TransactionSide`, `TransactionStatus` — all consolidated in `Transaction.kt`
- `TransactionRepository` interface + `TransactionRepositoryImpl` co-located in same file
- `GetTransactionsUseCase` — single-line delegation

**Tests:**
- ~~`GetTransactionsUseCaseTest`~~ — deleted; single-line delegation with no logic, not worth testing

---

### Task 3 — Room database ✅
**Commit:** `feat: add Room database with transaction and remote key entities`

- `TransactionEntity` with `@Embedded` for nested types (prefixed columns)
- `RemoteKeyEntity` — single fixed-ID row storing seed + nextPage
- `TransactionDao`, `RemoteKeyDao`, `AppDatabase`

---

### Task 4 — Network layer ✅
**Commit:** `feat: add Retrofit service, response DTOs, and domain mappers`

- All DTOs consolidated in `TransactionResponseDto.kt`
- `TransactionService` returns `TransactionResponseDto` directly (not `Response<T>`)
- `TransactionMapper` — `toEntity()` and `toDomain()` extension functions

---

### Task 5 — RemoteMediator + Repository ✅
**Commits:**
- `feat: add TransactionRemoteMediator, TransactionLocalDataSource, and tests`
- `refactor: centralise error handling and simplify data layer`

**Planned:**
- ~~`TransactionLocalDataSource` facade between mediator and DB~~
- ~~`PaginationState` as mediator-facing type~~
- ~~`TransactionRemoteDataSource`~~
- Mediator owning refresh/append logic directly

**Actual:**
- `TransactionRemoteMediator` reduced to a thin callback dispatcher (`onInitialize`, `onRefresh`, `onAppend` lambdas)
- `TransactionRepositoryImpl` owns all pagination logic — `refresh()` and `append()` directly access `AppDatabase`
- `safeApiCall` top-level function maps all network exceptions to typed `ApiError`
- `ApiError` sealed class: `NetworkError`, `HttpError(code)`, `UnknownError`
- Seed is `null` on every REFRESH — fresh data on every cold start and PTR

---

### Task 6 — Koin DI modules ✅
**Commit:** `feat: add TransactionRepository implementation and Koin DI modules`

- `NetworkModule`, `DatabaseModule`, `RepositoryModule`, `UseCaseModule`, `ViewModelModule`
- `BASE_URL` in `gradle.properties` via `BuildConfig`
- `AppDatabase.create(context)` factory on companion object

---

### Task 7 — ViewModel ✅
**Commit:** `feat: add TransactionListViewModel with Paging 3 integration`

- `val transactions: Flow<PagingData<Transaction>>` — `.cachedIn(viewModelScope)`
- ~~`val uiState: StateFlow<TransactionUiState>`~~ — dropped; Paging 3 load state covers all UI states, a parallel `StateFlow` would duplicate and risk drift

---

### Task 8 — Compose UI ✅
**Commits:**
- `feat: add TransactionListScreen with Compose UI`
- `fix: stabilise paging and list rendering`
- `feat: add pull-to-refresh, seed renewal on startup, and startup UX`

- `PullToRefreshBox` as outer container; `isRefreshing` scoped to `loadState.mediator?.refresh`
- Full-screen `CircularProgressIndicator` for cold start; PTR overlay for pull-to-refresh
- `Snackbar` with Retry for PTR errors; `ErrorView` with Retry for cold-start errors
- `AppListItem` generic component; `AppListItemPlaceholder` skeleton
- `Throwable?.toUserMessage()` composable extension for centralised error strings
- `AmountFormatter`, `DateFormatter` utilities

---

### Task 9 — Tests ✅
**Commit:** `test: add SafeApiCallTest, TransactionRepositoryImplTest, and gap coverage`

**Planned test scope:** `RemoteMediator` + `UseCase`

**Actual test scope:**
- `TransactionMapperTest` — `toEntity()` and `toDomain()` including nullable fields and enum parsing
- `TransactionRemoteMediatorTest` — routing (PREPEND/REFRESH/APPEND), `initialize()`, error propagation
- `SafeApiCallTest` — all 5 exception branches
- `TransactionRepositoryImplTest` — `refresh`/`append` success and error paths, `MAX_PAGE` boundary, guard conditions

`refresh`/`append` made `internal` for direct testing. `withTransaction` intercepted via `mockkStatic` + `secondArg`.

---

### Task 10 — Part 2 presentation answers ⏳
**Commit:** `docs: add Part 2 presentation answers to COLLABORATION.md`

- Fill in the "Part 2 — Presentation Answers" section of `COLLABORATION.md`
- Context, architecture diagram, good practices, development strategy, future-proofing

---

### Task 11 — Pull request ⏳
**Action:** Open PR from `feature` to `main`

- PR description with summary of decisions
- Short video of the running app (per README recommendation)
