# Ktor + Netty

**Why:** coroutine-native, small composable plugin model, same vendor and
serialization stack as the multiplatform Ktor client. One mental model
client→server.

**Not:**
- **Spring Boot** — heavy, annotation-magic, slow startup, opinionated DI
  we don't want.
- **Http4k / Javalin / Vert.x** — fine, but no client/server symmetry.

**Cost:** plugins are explicit (no auto-config). New devs read
`Application.kt` to see what's installed — that's the point.
