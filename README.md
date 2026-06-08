# Android Transaction Viewer

A Kotlin + Jetpack Compose Android app that fetches a paginated list of transactions from a REST API and displays them with offline support.

Built as a 2-day senior Android engineering exercise.

---

## What it does

- Fetches paginated transactions from a remote API using **Paging 3** with a `RemoteMediator`
- Displays `counterpartyName`, `amount`, `settledAt`, and `status` per transaction
- Caches pages locally with **Room** so previously loaded transactions remain accessible offline
- Pull-to-refresh renews the session seed and clears the local cache
- Handles network errors gracefully via a `safeApiCall` wrapper and a `ApiError` sealed class

## Tech stack

| Layer | Choice |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM, single-module, layer-first packages |
| Pagination | Paging 3 + `RemoteMediator` |
| Networking | Retrofit + OkHttp + Kotlin Serialization |
| Local storage | Room |
| DI | Koin |
| Tests | JUnit 4 + MockK |

## Architecture decisions

Key decisions are documented as ADRs under [`docs/adr/`](docs/adr/):

- [ADR-0001 — Koin over Hilt](docs/adr/0001-dependency-injection-koin.md)
- [ADR-0002 — Retrofit + OkHttp + Kotlin Serialization](docs/adr/0002-networking-retrofit-okhttp-kotlin-serialization.md)
- [ADR-0003 — MVVM over MVI](docs/adr/0003-ui-pattern-mvvm.md)
- [ADR-0005 — Pagination termination via API contract](docs/adr/0005-pagination-termination-max-page-contract.md)

AI usage and design rationale are documented in [`COLLABORATION.md`](COLLABORATION.md).

## How to build

**Prerequisites:** Android Studio Meerkat (2024.3.1)+, JDK 17, Android SDK API 24+

```bash
./gradlew assembleDebug
```

Or open in Android Studio and run the `app` configuration on a device or emulator.
