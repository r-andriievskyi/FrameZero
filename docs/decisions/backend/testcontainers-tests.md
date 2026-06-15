# Testcontainers Postgres for tests

**Why:** repo tests run against the **real Postgres binary** in a throwaway
container (`:server:test`), so Flyway migrations and Exposed queries are
exercised on the same engine prod runs. Catches Postgres-only behaviour
(constraints, `JSONB`, partial indexes, SQL dialect) that an emulated DB would
wave through. `testImplementation` only.

**Not:**
- **H2 (Postgres mode)** — what we started on: fast and Docker-free, but
  *not* Postgres. It silently parses non-portable SQL differently and hides
  exactly the SQL-layer bugs we want to catch. Dropped.
- **Mocked repos** — most regressions live at the SQL layer; mocks hide them.
- **Embedded Postgres (Zonky)** — real-ish, but slower to spin up and not the
  same packaging as prod.

**Cost:** tests need **Docker running** (`:server:test` is skipped/failing
without it). On macOS the Docker socket sometimes needs
`TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE` — see `server/build.gradle.kts`.
