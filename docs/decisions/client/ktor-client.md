# Ktor Client

**Why:** matches the Ktor server — same `kotlinx.serialization` wire shapes
(kept in sync, see [duplicated-dtos](../backend/duplicated-dtos.md)), same
idioms. Pluggable engine per platform: OkHttp on Android, Darwin on iOS.
Coroutines end-to-end.

**Not:**
- **Retrofit** — Android/JVM only, annotation-based, doesn't run in
  `commonMain`.
- **OkHttp directly** — JVM only, no iOS, no coroutine-native API.
- **Apollo / GraphQL clients** — we ship REST.

**Cost:** Ktor client APIs evolve quickly; treat version bumps as a
small migration each time.
