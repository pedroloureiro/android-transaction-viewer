# ADR 0001: Use Koin for Dependency Injection

**Status:** Accepted  
**Date:** 2026-05-30

## Context

The project needs a DI framework. The two main candidates for Android are Hilt (Google-recommended, compile-time safe) and Koin (runtime DI, lighter setup).

## Decision

Use **Koin**.

## Rationale

- Single-module, small-to-medium app — Koin's runtime overhead is negligible at this scale.
- Koin requires no annotation processing or kapt/KSP plugin setup, reducing build complexity.
- Developer is familiar with Hilt but Koin's DSL is readable and straightforward to ramp up on.
- The scope of this exercise does not benefit from Hilt's compile-time safety guarantees in a meaningful way.

## Alternatives Considered

**Hilt** — Google's recommended DI for Android. Compile-time safe, catches missing bindings at build time. Requires the `com.google.dagger:hilt-android` plugin + KSP/kapt. Preferred for large, multi-module production apps.

## Consequences

- Reviewers familiar with Hilt as the Android standard may ask "why Koin?" — this ADR is the answer.
- DI graph errors surface at runtime rather than compile time.
- If the project grows into multi-module, Koin scales reasonably but Hilt would be the stronger choice.
