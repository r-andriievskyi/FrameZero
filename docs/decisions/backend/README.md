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
- [shared-dtos](shared-dtos.md) — wire contract
- [h2-tests](h2-tests.md) — test database
