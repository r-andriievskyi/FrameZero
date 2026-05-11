# 0004 — Exposed + HikariCP for data access

- **Status:** Accepted
- **Date:** 2026-05-11

## Context

We need to talk to Postgres from Kotlin. The access layer must:

- Compose with coroutines so route handlers can stay non-blocking
  (`suspendTransaction { … }`).
- Express joins, filters, and updates in Kotlin without writing string SQL
  for every query.
- Stay close to SQL — we are happy reasoning in tables, columns, and
  transactions, and we explicitly do not want a heavy ORM that hides query
  shape behind lazy graphs.
- Pool connections sanely; the JVM cost of opening a JDBC connection per
  request is a non-starter.

## Decision

Use **JetBrains Exposed (DSL)** for the query layer and **HikariCP** as the
connection pool.

Pool config (see `DatabaseFactory`): `maximumPoolSize = 10`, `autoCommit =
false` (Exposed manages transactions explicitly), isolation
`READ_COMMITTED`.

## Alternatives

- **Hibernate / JPA** — full ORM with lazy loading, dirty tracking, an L1/L2
  cache, and an entity lifecycle. Powerful, but the failure modes (N+1
  queries, `LazyInitializationException`, mismatch between detached/managed
  state) cost more attention than they save here.
- **jOOQ** — excellent type-safe SQL, but a paid licence for the Postgres
  edition and an extra code-generation step in the build. Worth revisiting if
  we outgrow Exposed's expressiveness.
- **Raw JDBC** — no abstraction tax, but every query becomes hand-written
  SQL plus manual `ResultSet` mapping. Repetitive and error-prone for a
  growing schema.
- **Ktorm / SqlDelight server-side** — viable Kotlin-native options, but
  Exposed is the de facto JetBrains stack and integrates with coroutines
  and Hikari out of the box.
- **Other connection pools (Tomcat JDBC, c3p0)** — Hikari is the default
  choice for a reason: smallest, fastest, least surprising.

## Consequences

- Queries are written in a typed Kotlin DSL; refactors that rename a column
  break the build, not production.
- We retain visibility into the SQL we issue — no lazy graphs, no implicit
  fetches. If a query is slow, it is slow because of what we wrote.
- Exposed transactions are coroutine-friendly via `suspendTransaction`, so
  the route → service → DB call chain is suspending end-to-end.
- HikariCP handles connection lifecycle; we tune pool size as load profile
  becomes clearer (currently 10, deliberately conservative).
- Exposed's DAO API exists but we do not use it; we stay on the DSL to keep
  query shape explicit.
