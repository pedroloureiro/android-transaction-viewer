# Implementation Roadmap

## Architecture Decisions Summary

| Decision | Choice | ADR |
|---|---|---|
| DI framework | Koin | [ADR-0001](docs/adr/0001-dependency-injection-koin.md) |
| Networking | Retrofit + OkHttp + Kotlin Serialization | [ADR-0002](docs/adr/0002-networking-retrofit-okhttp-kotlin-serialization.md) |
| UI pattern | MVVM + structured `UiState` | [ADR-0003](docs/adr/0003-ui-pattern-mvvm.md) |
| Session seed | Persistent, never reset in-app | [ADR-0004](docs/adr/0004-seed-as-permanent-session-identifier.md) |
| Pagination end | `MAX_PAGE = 10_000` per API contract | [ADR-0005](docs/adr/0005-pagination-termination-max-page-contract.md) |
| Module structure | Single module, layer-first packages | — |
| Room nested objects | `@Embedded` for amount, initiator, bankAccount | — |
| Page size | 20 results per request | — |
| Test scope | `RemoteMediator` + `UseCase` | — |

## Package Structure

```
com.qonto.transactionviewer
├── data/
│   ├── local/
│   │   ├── dao/
│   │   ├── entity/
│   │   └── database/
│   ├── remote/
│   │   ├── dto/
│   │   ├── mapper/
│   │   └── service/
│   ├── mediator/
│   └── repository/
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
└── ui/
    ├── screen/
    └── viewmodel/
```

---

## Tasks

### Task 1 — Project dependencies and build configuration
**Commit:** `feat: initialise build configuration and project dependencies`

Add to `libs.versions.toml` and `build.gradle.kts`:
- Koin (`koin-android`, `koin-androidx-compose`)
- Retrofit + OkHttp + logging interceptor
- Kotlin Serialization plugin + `kotlinx-serialization-json` + Retrofit converter
- Room (`room-runtime`, `room-ktx`, `room-paging`) + KSP processor
- Paging 3 (`paging-runtime`, `paging-compose`)
- Test dependencies: `kotlinx-coroutines-test`, `mockk`, `koin-test`, `room-testing`

Also enable KSP plugin and Kotlin Serialization plugin in `build.gradle.kts`.

---

### Task 2 — Domain layer
**Commit:** `feat: add domain layer with Transaction model, use case, and repository interface`

- `domain/model/Transaction.kt` — pure Kotlin data class (no Android, no serialization annotations)
- `domain/model/Amount.kt`
- `domain/repository/TransactionRepository.kt` — interface returning `Flow<PagingData<Transaction>>`
- `domain/usecase/GetTransactionsUseCase.kt` — delegates to repository, injectable

**Tests:**
- `GetTransactionsUseCaseTest` — verifies delegation and any transformation logic

---

### Task 3 — Room database
**Commit:** `feat: add Room database with transaction and remote key entities`

- `data/local/entity/TransactionEntity.kt` — mirrors domain model, uses `@Embedded` for `AmountEmbedded`, `InitiatorEmbedded?`, `BankAccountEmbedded`
- `data/local/entity/RemoteKeyEntity.kt` — stores `nextPage: Int` and `seed: String`
- `data/local/dao/TransactionDao.kt` — `@Insert`, `@Query`, `@Delete`
- `data/local/dao/RemoteKeyDao.kt` — `@Insert`, `@Query`, `@Delete`
- `data/local/database/AppDatabase.kt` — `@Database`, exports schema

---

### Task 4 — Network layer
**Commit:** `feat: add Retrofit service, response DTOs, and domain mappers`

- `data/remote/dto/TransactionResponseDto.kt` — `@Serializable`, mirrors API shape
- `data/remote/dto/TransactionDto.kt`, `AmountDto.kt`, `InitiatorDto.kt`, `BankAccountDto.kt`, `PaginationInfoDto.kt`
- `data/remote/service/TransactionService.kt` — Retrofit interface: `suspend fun getTransactions(results, page, seed?): TransactionResponseDto`
- `data/remote/mapper/TransactionMapper.kt` — `TransactionDto → Transaction` (domain), `TransactionDto → TransactionEntity`

---

### Task 5 — RemoteMediator
**Commit:** `feat: add TransactionRemoteMediator with seed persistence and pagination`

- `data/mediator/TransactionRemoteMediator.kt`
  - `REFRESH`: clears transactions + remote keys, fetches page 1 with no seed, persists returned seed + `nextPage = 2`
  - `APPEND`: reads persisted seed + next page from `RemoteKeyDao`, fetches, persists new key
  - End condition: `nextPage > MAX_PAGE` (10,000)
  - Returns `MediatorResult.Error` on network failure

**Tests (`TransactionMapperTest`):**
- `toEntity()` maps all fields correctly including nullable initiator
- `toDomain()` parses valid side and status strings to enums
- `toDomain()` with unknown side string throws `IllegalArgumentException`
- `toDomain()` with unknown status string throws `IllegalArgumentException`

**Tests (`TransactionRemoteMediatorTest`):**
- REFRESH clears DB and persists seed from first response
- APPEND uses persisted seed and increments page
- APPEND returns `endOfPaginationReached = true` when `nextPage > MAX_PAGE`
- REFRESH on network failure returns `MediatorResult.Error`
- APPEND on network failure returns `MediatorResult.Error`

---

### Task 6 — Repository implementation and Koin modules
**Commit:** `feat: add TransactionRepository implementation and Koin DI modules`

- `data/repository/TransactionRepositoryImpl.kt` — implements domain interface, constructs `Pager` with `RemoteMediator` + `PagingSource` from Room DAO
- `di/NetworkModule.kt` — OkHttp, Retrofit, `TransactionService`
- `di/DatabaseModule.kt` — `AppDatabase`, DAOs
- `di/RepositoryModule.kt` — `TransactionRepositoryImpl` bound to `TransactionRepository`
- `di/UseCaseModule.kt` — `GetTransactionsUseCase`
- `di/ViewModelModule.kt` — `TransactionListViewModel`
- Application class with `startKoin { ... }`

---

### Task 7 — ViewModel
**Commit:** `feat: add TransactionListViewModel with Paging 3 integration`

- `ui/viewmodel/TransactionListViewModel.kt`
  - `val transactions: Flow<PagingData<Transaction>>` — from use case, `.cachedIn(viewModelScope)`
  - `val uiState: StateFlow<TransactionUiState>` — `Loading | Success | Error`
- `ui/viewmodel/TransactionUiState.kt` — sealed class

---

### Task 8 — Compose UI
**Commit:** `feat: add TransactionListScreen with Compose UI`

- `ui/screen/TransactionListScreen.kt`
  - `collectAsLazyPagingItems()` on `transactions`
  - `LazyColumn` with `TransactionItem` composable per item
  - Each item: `counterpartyName` + formatted `amount` on one line, `settledAt` + `status` below
  - `loadState.refresh is Loading` → centered `CircularProgressIndicator`
  - `loadState.append is Loading` → bottom-of-list loading item
  - `loadState.refresh is Error` → error message + Retry button
  - `loadState.append is Error` → inline retry at list bottom
- `ui/screen/TransactionItem.kt` — stateless composable
- Wire `MainActivity` to the screen

---

### Task 9 — AI collaboration and architecture documentation
**Commit:** `docs: add AI collaboration summary and architecture documentation`

- `COLLABORATION.md` — answers the README Part 2 questions: context, architecture, good practices, development strategy, future-proofing
- Covers AI usage: what was used for, how it was prompted, where it was challenged

---

## Fix / Refactor commits (as needed)

Appear inline when required. Examples:
- `fix: correct seed not persisted across process death`
- `refactor: extract paging constants to PagingConfig object`
- `fix: handle null settledAt in UI date formatting`
- `docs: update roadmap with revised pagination decision`
