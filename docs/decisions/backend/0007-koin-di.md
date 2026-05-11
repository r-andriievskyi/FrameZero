# 0007 — Koin for dependency injection

- **Status:** Accepted
- **Date:** 2026-05-11

## Context

The server wires together a small number of long-lived collaborators per
feature: a config object, a repository, a service, sometimes a JWT helper.
The clients (Android, iOS, Desktop) already use Koin in `composeApp` and
`shared` to wire ViewModels and repositories.

We want one DI story across the whole codebase so a contributor doesn't have
to learn two container idioms when they cross the client/server boundary.

## Decision

Use **Koin** (`koin-ktor`) on the server, registered via `install(Koin) { … }`.

Each feature ships its own module function (`authModule`, `productionModule`,
`taskModule`, `scheduleModule`, `notificationModule`, `dashboardModule`)
which the application composes at startup.

## Alternatives

- **Hilt / Dagger** — compile-time DI with strong guarantees, but Android-
  centric, KSP-driven, and overkill for a service with this few wiring
  points. Adds build complexity for no daily benefit.
- **Kodein** — comparable to Koin; Koin is what the rest of the stack uses,
  so picking it removes a choice rather than adding one.
- **Manual constructor wiring** — viable for a service this small, and we
  may yet drift toward it in places. The risk is that route handlers grow
  ad-hoc factory functions over time. Koin gives us a single place to look.
- **Spring's container** — would force adopting Spring, see ADR 0002.

## Consequences

- One DI mental model spans `shared`, `composeApp`, and `server`.
- Wiring errors are runtime errors, not compile-time errors; mitigated by
  starting the application in tests, which exercises every module's
  resolution graph.
- Feature modules are self-contained: adding a new feature means adding a
  `<feature>Module()` function and registering it in `Application.module`,
  not threading constructors through the call graph.
- Koin's reflection-light, runtime-resolved approach has a small startup
  cost. Negligible at our scale.
