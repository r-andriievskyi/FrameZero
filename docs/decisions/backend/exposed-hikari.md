# Exposed (DSL) + HikariCP

**Why:** typed Kotlin SQL DSL (column renames break the build), suspending
transactions (`suspendTransaction`) keep the route→service→DB chain
non-blocking. Hikari is the default JVM connection pool for a reason.

**Not:**
- **Hibernate/JPA** — lazy loading, N+1, detached/managed lifecycle —
  too much hidden behaviour.
- **jOOQ** — paid Postgres edition + codegen step. Revisit if Exposed
  hits a wall.
- **Raw JDBC** — repetitive, ResultSet mapping by hand.

**Cost:** Exposed DAO API exists; we deliberately stick to the DSL to
keep query shape explicit.
