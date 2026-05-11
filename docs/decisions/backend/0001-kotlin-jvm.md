# 0001 — Kotlin on the JVM as backend language

- **Status:** Accepted
- **Date:** 2026-05-11

## Context

The product is a Kotlin Multiplatform app: Android, iOS, Desktop, and Web
clients all share `commonMain` code, including DTOs and domain types in the
`shared` module. The backend is a single small service owned by an Android
engineer.

The backend's job is to serve typed JSON over HTTP, persist data, and
authenticate users. There is no requirement for sub-millisecond latency or
extreme horizontal scale.

## Decision

Use **Kotlin on the JVM** for the server.

The `server` module depends directly on `shared`, so request/response types,
enums, and constants are defined exactly once and consumed unchanged on both
ends of the wire.

## Alternatives

- **Node.js / TypeScript** — would require duplicating every DTO and re-deriving
  enum semantics. Loses the strongest leverage we have: a typed contract that
  the compiler enforces on both client and server.
- **Go** — fast, simple deployment, but again forces DTO duplication and a
  second language for the team to maintain.
- **Kotlin/Native server** — immature ecosystem for HTTP, JDBC, and migrations.
  No payoff for a service that isn't latency-critical.

## Consequences

- One language across the entire stack; the same engineer can move freely
  between client and server.
- DTOs live in `shared/commonMain` and are imported by `server`; the wire
  format cannot drift silently.
- We accept JVM startup time and memory overhead. For a single long-running
  service this is irrelevant; if we ever need short-lived functions, we will
  revisit (e.g. GraalVM native-image or a Kotlin/Native rewrite of a hot path).
- Build times are dominated by Kotlin compilation; mitigated by the Gradle
  configuration and build cache being on by default.
