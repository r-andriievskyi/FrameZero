# 0009 — H2 in PostgreSQL mode for integration tests

- **Status:** Accepted
- **Date:** 2026-05-11

## Context

Server tests need to exercise real SQL: route → service → repository →
database. Mocking the database hides the most interesting class of bugs
(query syntax errors, constraint violations, migration mistakes). We also
want tests to be fast, hermetic, and runnable on a contributor laptop with
no setup.

Production runs on PostgreSQL (ADR 0003).

## Decision

Run the server test suite against **H2** in PostgreSQL compatibility mode,
in-memory, with Flyway migrations applied on each test setup.

H2 is a `testImplementation`-only dependency; it is never on the production
classpath.

## Alternatives

- **Testcontainers + real Postgres** — the most faithful option; we will
  adopt it for any test that exercises Postgres-specific behaviour (JSONB
  operators, advisory locks, partial indexes). Today, requiring Docker on
  every contributor's laptop and on CI is a friction tax we don't want for
  the bulk of route/repository tests.
- **Mocked repositories** — fast but useless: most regressions in this kind
  of code are at the SQL layer.
- **Embedded Postgres (Zonky / OpenTable)** — heavier than H2, slower
  startup, and still not the same binary as production. If we are going to
  pay a startup cost, we'd rather pay it for a real Postgres via
  Testcontainers.
- **Shared dev database** — non-hermetic, parallel-test-hostile, easy to
  poison.

## Consequences

- Tests run in milliseconds and require no external services. CI is simple.
- We accept that H2-PG mode is *not* Postgres. A migration or query that
  passes against H2 can still fail against Postgres. Mitigations:
  - Keep migrations to portable ANSI SQL where possible.
  - When a feature needs Postgres-specific syntax, gate that test on a
    real Postgres (future: Testcontainers) and skip H2.
  - Production deploys run migrations on a real Postgres; failures there
    block the deploy.
- The test DB is recreated per run; no fixture pollution between tests.
- Path forward: when the server's surface grows enough that H2 fidelity
  becomes a liability, add a Testcontainers-backed second test source set
  rather than replacing H2 wholesale — keep the fast loop fast.
