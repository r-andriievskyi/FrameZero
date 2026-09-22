# Self-registering plugins (sink + multibinding)

**Why:** cross-cutting concerns (logging, analytics, performance, sign-out cleanup) use
a sink interface + facade pattern. Implement the sink (`LogSink`, `AnalyticsSink`,
`PerformanceSink`, `SessionCleaner`) and annotate it `@ContributesIntoSet(AppScope::class)`;
the facade (`Logger`, `Analytics`, `Performance`, `SessionManager`) injects `Set<Sink>`
and fans out to each under its own `runCatching`. Adding a backend is one annotation,
no facade edit. Reference: `shared/integrations/firebase/`.

**Not:**
- **Facade with hard-coded backends** — every new sink edits the facade and couples it
  to each implementation.
- **One global event bus** — looser typing, harder to see who handles what.

**Cost:** an extension point that is legitimately empty in some build has to say so with
`@Multibinds(allowEmpty = true)`, otherwise Metro rejects the graph. `CoreMultibindings`
declares exactly the three telemetry sets, because demo builds ship without Firebase.
`SessionCleaner` is deliberately left out: an empty cleaner set would mean sign-out had
silently stopped wiping local state, and that should not compile. Note this depends on
`SessionManager.cleaners` having **no default value** — Metro reads a defaulted parameter as an
optional binding and would quietly fall back to it, which is precisely the silent failure the
multibinding is supposed to rule out. Each sink still runs
under its own `runCatching` so one misbehaving backend can't take the others down.
