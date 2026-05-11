# H2 (PostgreSQL mode) for tests

**Why:** in-memory, ms-fast startup, Flyway migrations applied per run,
no Docker on contributor laptops. `testImplementation` only.

**Not:**
- **Mocked repos** — most regressions are at the SQL layer; mocks hide
  exactly the bugs we want to catch.
- **Testcontainers + real Postgres** — most faithful, but Docker
  requirement adds friction. Path forward: add a second test source set
  for Postgres-specific behaviour, keep the H2 fast loop fast.
- **Embedded Postgres (Zonky)** — slower, still not the prod binary.

**Cost:** H2-PG mode is *not* Postgres. Stay close to portable SQL;
prod migrations catch the rest.
