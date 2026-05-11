# 0003 — PostgreSQL as the primary datastore

- **Status:** Accepted
- **Date:** 2026-05-11

## Context

The data model is relational and well-understood: users, productions,
production members, tasks, schedule events, notifications, refresh tokens.
Most queries cross at least one foreign key (e.g. "tasks for production X
visible to user Y"). We need transactional updates that touch multiple
tables, joins on indexed columns, and ad-hoc reporting on schedule and
production data.

Operational constraints: a small team, no dedicated DBA. The database has to
be boring, well-understood, and supported by every cloud provider and every
local dev setup.

## Decision

Use **PostgreSQL** as the only production datastore.

All schema is owned by the server and managed via Flyway migrations
(see ADR 0005). Connection pooling via HikariCP, query layer via Exposed
(see ADR 0004).

## Alternatives

- **MySQL / MariaDB** — viable, but Postgres has stronger semantics for the
  things we are likely to need next: enums, JSONB columns, partial indexes,
  generated columns, `LISTEN`/`NOTIFY`. Same operational profile.
- **SQLite on the server** — single-writer concurrency model would bite as
  soon as a second instance is spun up. Acceptable as an embedded client DB,
  not as a server datastore.
- **MongoDB / DynamoDB / other document stores** — our data is heavily
  relational. Modelling production membership and schedule queries on a
  document store would push joins into application code and make consistency
  a per-query exercise.
- **Firebase / Supabase** — would couple the schema to a vendor's auth and
  RLS model. We already have a typed Kotlin server; the value-add of a BaaS is
  small and the lock-in is large.

## Consequences

- Strong transactional guarantees, mature tooling, predictable behaviour.
- We carry the operational cost of running Postgres (or paying a managed
  provider). Acceptable.
- Schema is owned by us, not by a provider; migrations are versioned in the
  repo (ADR 0005).
- Tests run against H2 in PostgreSQL compatibility mode (ADR 0009). We accept
  that some Postgres-only features (JSONB operators, partial indexes,
  `RETURNING`) may need to be exercised against a real Postgres in CI before
  shipping.
