# Flyway

**Why:** versioned SQL files in the repo, applied on app start, reviewable
in PRs. Migrations are immutable once merged — fix forward with `V<n+1>`.

**Not:**
- **Liquibase** — XML/YAML changesets; we'd rather review literal SQL.
- **`SchemaUtils.createMissingTablesAndColumns`** — can't rename, drop,
  or backfill. Useless for production evolution.
- **Hand-rolled runner** — solving a solved problem.

**Cost:** any Postgres-only migration must be exercised against real
Postgres in CI; H2 (test DB) won't always parse it.
