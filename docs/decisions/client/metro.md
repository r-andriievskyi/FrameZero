# Metro

**Why:** compile-time DI for `commonMain`. A missing binding, a duplicate one, or a
fake leaking into a production build is a compilation error, not a crash at app
start. It is a Kotlin compiler plugin, so there is no KSP fan-out across
`android`/`iosArm64`/`iosSimulatorArm64` the way `:shared:database` needs for Room.

**Not:**
- **Koin** — what this replaced. Runtime resolution meant wiring errors surfaced at
  startup, and the self-registering plugin pattern had no compile-time check at all
  (see [self-registering-plugins](self-registering-plugins.md)).
- **Hilt/Dagger** — Android-only, doesn't run in `commonMain`.
- **kotlin-inject** — comparable and multiplatform, but no first-class aggregation;
  every binding would have to be listed on the graph by hand.

**Cost:**
- The **server still uses Koin** (`koin-ktor`), so the repo no longer has one DI
  mental model end to end. Accepted: the server is a single JVM module with a flat
  graph, where runtime resolution costs little, and `koin-ktor` integrates with Ktor's
  plugin model in a way Metro does not.
- Demo/prod can no longer be a load-order override. Each platform declares two graphs
  that exclude the other flavor's bindings — see `AndroidAppGraphs.kt` / `IosAppGraphs.kt`.
  Deliberately **without** `replaces`, so a missing exclusion is a duplicate-binding
  compile error instead of a production build quietly serving demo data.
- Metro cannot see another module's `internal` declarations when it merges the graph,
  so cross-module contributions are either public types or a public `@BindingContainer`
  wrapping internal ones (`DemoBindings` does the latter for all the demo fakes).

Day-to-day usage — annotations, patterns and traps — is in
[`docs/dependency-injection.md`](../../dependency-injection.md).
