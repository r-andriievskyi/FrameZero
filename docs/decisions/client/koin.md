# Koin

**Why:** works in `commonMain` (KMP-friendly), no codegen, same container
on the server (`koin-ktor`). One DI mental model across the entire repo.

**Not:**
- **Hilt/Dagger** — Android-only annotation processors, KSP build cost,
  doesn't run in `commonMain`.
- **Manual constructor wiring** — fine until 5+ features, then noisy.
- **Kodein** — comparable, but Koin is what we already use elsewhere.

**Cost:** runtime resolution = wiring errors at app start, not compile.
Mitigated by exercising the graph in a startup test.
