# ADR 0002: Use Retrofit + OkHttp + Kotlin Serialization for Networking

**Status:** Accepted  
**Date:** 2026-05-30

## Context

The app needs an HTTP client to fetch paginated transaction data from the Transactions API.

## Decision

Use **Retrofit** (HTTP abstraction) + **OkHttp** (HTTP client) + **Kotlin Serialization** (JSON parsing).

## Rationale

- Retrofit is the de-facto standard Android HTTP library — familiar, well-documented, and expected by Android reviewers.
- OkHttp is Retrofit's default underlying client, giving connection pooling, interceptors, and logging out of the box.
- Kotlin Serialization is the Kotlin-native choice: no reflection, compile-time safe, works seamlessly with data classes and kotlinx.serialization annotations.
- Consistent with a fully Kotlin-first stack (vs Gson which is Java-based, or Moshi which adds another dependency for little gain here).

## Alternatives Considered

- **Gson** — simple, widely used, but Java-based and relies on reflection.
- **Moshi** — Kotlin-friendly, KSP-based codegen, but adds another dependency when Kotlin Serialization is already first-party.
- **Ktor** — Kotlin-native HTTP client, but introduces more unfamiliar API surface vs Retrofit for a time-boxed exercise.

## Consequences

- Requires the `kotlinx-serialization` Gradle plugin and the Retrofit Kotlin Serialization converter.
- JSON field naming mismatches must be handled via `@SerialName` annotations (explicit, not magic).
