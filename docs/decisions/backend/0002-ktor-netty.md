# 0002 — Ktor + Netty as the HTTP framework

- **Status:** Accepted
- **Date:** 2026-05-11

## Context

We need an HTTP server that:

- Speaks idiomatic Kotlin (coroutines, not blocking thread-per-request).
- Has a small, composable plugin model (auth, content negotiation, CORS, rate
  limit, status pages, call logging, call IDs).
- Shares its serialization stack with the multiplatform clients so DTOs
  defined in `shared` can be used directly on both sides.

The same vendor (JetBrains) already ships the `Ktor` HTTP **client** that the
mobile and desktop apps use. Aligning client and server on one stack means one
mental model and one set of release notes to track.

## Decision

Use **Ktor 3.x with the Netty engine**.

Wired plugins: `ContentNegotiation` (kotlinx.serialization), `Authentication`
(JWT), `CORS`, `CallId`, `CallLogging`, `StatusPages`, `RateLimit`.

## Alternatives

- **Spring Boot / WebFlux** — feature-rich but heavyweight. Annotation-driven
  magic, slow startup, and a configuration surface that dwarfs the actual
  service. Pulls in a Java-first idiom and an opinionated DI container we
  don't want.
- **Http4k / Javalin / Vert.x** — viable but none of them give us the
  client/server symmetry that Ktor does.
- **Spring MVC (blocking)** — same downsides as Boot, plus no native coroutine
  story.

## Consequences

- Coroutine-native request handling; suspend functions are first-class in
  routes, services, and DB transactions (`suspendTransaction`).
- Engine swap is trivial if we ever need it: `embeddedServer(Netty, …)` →
  `embeddedServer(CIO, …)` is one line. Locks us in only at the edge.
- Plugin DSL is explicit; there is no auto-configuration. New developers have
  to read `Application.kt` to know what is installed, but nothing is hidden.
- We lose the Spring ecosystem (Actuator, Spring Security, Spring Data). For
  the scope of this service that is a feature, not a loss.
