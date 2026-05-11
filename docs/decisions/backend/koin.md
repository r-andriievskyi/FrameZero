# Koin

**Why:** clients already use Koin in `shared` and `composeApp`. Same DI
story across the whole repo means no second container idiom to learn.

**Not:**
- **Hilt/Dagger** — Android-centric, KSP build cost, overkill.
- **Spring container** — would force adopting Spring (see ktor-netty).
- **Manual wiring** — fine for now, but route handlers tend to grow
  ad-hoc factories. Koin gives one place to look.

**Cost:** wiring errors are runtime, not compile-time. Mitigated by
test startup exercising every module's resolution graph.
