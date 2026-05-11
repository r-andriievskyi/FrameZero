# Backend ADRs

Architecture Decision Records for the `server/` module. Each ADR explains
*why this technology and not the obvious alternative*, from a system-design
perspective.

| #    | Decision                                             | Status   |
|------|------------------------------------------------------|----------|
| 0001 | [Kotlin on the JVM as backend language](0001-kotlin-jvm.md) | Accepted |
| 0002 | [Ktor + Netty as the HTTP framework](0002-ktor-netty.md)    | Accepted |
| 0003 | [PostgreSQL as the primary datastore](0003-postgresql.md)   | Accepted |
| 0004 | [Exposed + HikariCP for data access](0004-exposed-hikari.md) | Accepted |
| 0005 | [Flyway for schema migrations](0005-flyway-migrations.md)   | Accepted |
| 0006 | [Stateless JWT auth with bcrypt](0006-jwt-bcrypt-auth.md)   | Accepted |
| 0007 | [Koin for dependency injection](0007-koin-di.md)            | Accepted |
| 0008 | [Shared kotlinx.serialization DTOs as the wire contract](0008-shared-dtos.md) | Accepted |
| 0009 | [H2 in PostgreSQL mode for integration tests](0009-h2-tests.md) | Accepted |

## Format

Each ADR follows a compact template:

- **Context** — the forces and constraints in play
- **Decision** — what we picked
- **Alternatives** — what we rejected and why
- **Consequences** — what we now have to live with

ADRs are immutable once accepted. To change a decision, write a new ADR that
supersedes the old one and update the status here.
