# Ktor Client

**Why:** matches the Ktor server (same DTOs via `kotlinx.serialization`,
same idioms). Pluggable engine per platform: OkHttp on Android/JVM,
Darwin on iOS, JS on Web. Coroutines end-to-end.

**Not:**
- **Retrofit** — Android/JVM only, annotation-based, doesn't run in
  `commonMain`.
- **OkHttp directly** — JVM only, no iOS, no coroutine-native API.
- **Apollo / GraphQL clients** — we ship REST.

**Cost:** Ktor client APIs evolve quickly; treat version bumps as a
small migration each time.
