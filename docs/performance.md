# Performance: baseline profiles & startup benchmarks

Everything runs against the **demo flavor** (offline seed data, any credentials) — same anchors as the Maestro E2E
flows (`testTagsAsResourceId` resource-ids like `sign-in:email`, `productions:list`).

## What ships

- `androidApp/src/demoRelease/generated/baselineProfiles/baseline-prof.txt` — committed,
  AOT-compiles hot startup/journey paths on install. Prod releases ship the same file: the
  `prod` source set points its `baselineProfiles` dir at the demo one
  (`androidApp/build.gradle.kts`). Read the coverage caveat below before trusting it for prod.
- `ReportDrawnWhen` in `MainActivity` marks time-to-fully-drawn once the session resolves
  (same condition that releases the splash screen) — powers `timeToFullDisplayMs`.

No `startup-prof.txt`: `BaselineProfileGenerator.startup()` deliberately omits
`includeInStartupProfile`. That flag is an either/or, not an addition — it would route the
startup rules to a separate 2.4MB file instead of the baseline profile. Omitting it keeps the
rules in `baseline-prof.txt` (ART still AOT-compiles them) and gives up only dex layout
optimization.

### Coverage caveat: demo-generated, prod-shipped

The profile is generated on the demo flavor, which never executes Ktor or the real repository
impls. Measured on the committed profile: ~13.2k `androidx/compose` rules and 561 Decompose
(the bulk of startup, carries over to prod) but **1** `io/ktor` rule and **0** repository-impl
rules — prod's network/data cold paths are unwarmed. ~137 demo-only rules are dead in prod
(R8 strips the demo wiring). The measured win below was demo→demo; **prod's real gain is
unmeasured and will be lower.** Closing the gap means running the journey against a real
backend on the prod flavor.

## Module layout

- `:benchmarks` — `com.android.test` module targeting `:androidApp`. Prod variants are
  disabled there; benchmarks are demo-only by construction.
- The `androidx.baselineprofile` plugin derives two build types from `release`:
  `nonMinifiedRelease` (profile collection) and `benchmarkRelease` (measurement, R8 on,
  debug-signed, Firebase Perf disabled).

## Regenerate the baseline profile

Regenerate whenever a release meaningfully changes startup or the core journey
(sign-in → dashboard → productions list → production details), then commit the result:

```bash
# device or emulator connected via adb (rooted or API 33+; emulator is fine)
./gradlew -q :androidApp:generateDemoReleaseBaselineProfile
```

Output lands in `androidApp/src/demoRelease/generated/baselineProfiles/`. The journey lives in
`benchmarks/src/main/kotlin/.../baselineprofile/BaselineProfileGenerator.kt`.

## Run the startup benchmark

```bash
./gradlew -q :benchmarks:connectedDemoBenchmarkReleaseAndroidTest
```

Two tests in `StartupBenchmark`: `startupWithoutBaselineProfile` (`CompilationMode.None`)
vs `startupWithBaselineProfile` (`Partial(BaselineProfileMode.Require)`). Compare median
`timeToInitialDisplayMs` / `timeToFullDisplayMs` in the printed results (also under
`benchmarks/build/outputs/androidTest-results/`). The delta is what the committed profile
buys **on the demo flavor** (last run: median cold TTID 1868ms → 1609ms, ~14%). Emulator
numbers are noisy in absolute terms (`suppressErrors = EMULATOR` is set) but fine for
relative comparison; use a physical device for real figures.

## Gotchas

- **Never run the unqualified `generateBaselineProfile`** — aggregate tasks span demo+prod
  and break the one-BuildKonfig-flavor-per-invocation rule (see CLAUDE.md). Always use the
  `Demo…`-qualified tasks above; their names drive the BuildKonfig inference automatically.
- Benchmark/generation tasks build the app with the `demoRelease` BuildKonfig flavor:
  minified, demo data, no backend URL required.
- `google-services.json` is required (as for any `:androidApp` build).
- The baseline-profile Gradle plugin is on a 1.5 alpha: 1.4.x rejects AGP 9
  ("not a supported android module").
- Verify the profile ships: `unzip -l androidApp/build/outputs/apk/prod/release/*.apk | grep
  dexopt` after `assembleProdRelease` — expect `assets/dexopt/baseline.prof`.
