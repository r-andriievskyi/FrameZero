# Flyway

**Why:** versioned SQL files in the repo, applied on app start, reviewable
in PRs. Migrations are immutable once merged — fix forward with `V<n+1>`.

**Not:**
- **Liquibase** — XML/YAML changesets; we'd rather review literal SQL.
- **`SchemaUtils.createMissingTablesAndColumns`** — can't rename, drop,
  or backfill. Useless for production evolution.
- **Hand-rolled runner** — solving a solved problem.

**Cost:** migrations run on app start, so a bad one blocks boot — fix forward,
never edit a merged `V<n>`. Cheap to catch: repo tests apply every migration to
real Postgres ([testcontainers-tests](testcontainers-tests.md)).
