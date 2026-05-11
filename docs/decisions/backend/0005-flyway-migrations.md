# 0005 — Flyway for schema migrations

- **Status:** Accepted
- **Date:** 2026-05-11

## Context

Schema changes need to ship with the code that depends on them. Multiple
developers (and multiple environments — local dev, CI, staging, production)
must converge on the same schema state without anyone running ad-hoc SQL.

We also want migrations to be readable as SQL, reviewable in a pull request,
and reproducible from a clean database.

## Decision

Use **Flyway** with versioned SQL scripts in
`server/src/main/resources/db/migration/`, named `V<n>__<description>.sql`.

Migrations run automatically on application start (`DatabaseFactory.init`).
Applied migrations are immutable: never edit a `V<n>__…sql` file once it has
landed in `main`; add a new `V<n+1>` instead.

In dev mode (`io.ktor.development=true`) we set `baselineOnMigrate = true` so
a partially-seeded local DB doesn't block startup. In production this is off.

## Alternatives

- **Liquibase** — comparable feature set; uses XML/YAML/JSON changesets by
  default. We prefer reviewing literal SQL in PRs to reading a changeset DSL,
  and the rollback features of Liquibase are not features we want to rely on.
- **Exposed `SchemaUtils.createMissingTablesAndColumns`** — convenient in
  dev, but it cannot rename, drop, or backfill; production schema evolution
  needs explicit, ordered, reviewable steps. We never use it.
- **Hand-rolled migration runner** — solving a solved problem; no.
- **Dump-and-restore from staging** — non-reproducible and untracked.

## Consequences

- Every schema change lands as a reviewable SQL file alongside the Kotlin
  changes that depend on it.
- Migrations apply on startup, so deployments are self-contained: the
  artifact knows how to evolve the schema it expects.
- Immutability of applied migrations is a hard rule; if something is wrong,
  fix forward. Documented in `CLAUDE.md` for new contributors.
- Flyway's PostgreSQL module is bundled (`flyway-database-postgresql`).
  Tests run on H2 (ADR 0009) in PostgreSQL compatibility mode; if a
  migration uses a Postgres-only feature that H2 cannot parse, we either
  rephrase it portably or run that migration only against a real Postgres
  in CI.
