# Backend ADRs

Short notes on why the backend stack is what it is. One file per choice,
each ~10 lines. Read top-to-bottom for a stack tour.

- [kotlin-jvm](kotlin-jvm.md) — language & runtime
- [ktor-netty](ktor-netty.md) — HTTP framework
- [postgresql](postgresql.md) — datastore
- [exposed-hikari](exposed-hikari.md) — query layer & pool
- [flyway](flyway.md) — schema migrations
- [jwt-bcrypt](jwt-bcrypt.md) — auth
- [koin](koin.md) — DI
- [duplicated-dtos](duplicated-dtos.md) — wire contract (server owns its own copy)
- [testcontainers-tests](testcontainers-tests.md) — test database
