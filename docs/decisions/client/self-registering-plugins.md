# Self-registering plugins (sink + facade + bind)

**Why:** cross-cutting concerns (logging, analytics, sign-out cleanup) use a
sink interface + facade pattern. Implement the sink (`LogSink`, `AnalyticsSink`,
`SessionCleaner`) and register it with `single { … } bind <Sink>::class`; the
facade (`Logger`, `Analytics`, `SessionManager`) injects `getAll<Sink>()` and
fans out to each under its own `runCatching`. Adding a backend (e.g. Crashlytics)
is one `bind` line, no facade edit. Reference: `shared/integrations/firebase/`.

**Not:**
- **Facade with hard-coded backends** — every new sink edits the facade and
  couples it to each implementation.
- **One global event bus** — looser typing, harder to see who handles what.

**Cost:** wiring is runtime (Koin), so a forgotten `bind` is a silent no-op
rather than a compile error. Each sink runs under its own `runCatching` so one
misbehaving backend can't take the others down.

