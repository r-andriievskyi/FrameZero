# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

**Android**
```bash
./gradlew :composeApp:assembleDebug        # build debug APK
./gradlew :composeApp:installDebug         # install on connected device/emulator
```

**Desktop (JVM)**
```bash
./gradlew :composeApp:run
```

**Server (Ktor)**
```bash
./gradlew :server:run                      # starts on port 8080
```

**iOS** — open `iosApp/iosApp.xcodeproj` in Xcode and run from there.
The Gradle task `embedAndSignAppleFrameworkForXcode` runs as part of the Xcode
build; don't invoke it manually unless debugging a framework linking issue.

## Tests

```bash
./gradlew :composeApp:test                 # shared UI module tests
./gradlew :shared:test                     # shared library tests
./gradlew :server:test                     # Ktor server integration tests
./gradlew test                             # all modules
```

Run a single test class: `./gradlew :shared:test --tests "com.frame.zero.SharedCommonTest"`
(The `:server` module has no tests yet.)

## Architecture

A composite build, a native iOS wrapper, and a tree of Gradle modules:

| Module | Purpose |
|--------|---------|
| `build-logic/` | Composite build with Gradle convention plugins (not a regular module). |
| `shared/` | Multiplatform business logic; no UI. Holds shared `Constants`, domain models (`User`, `DomainError`, `Outcome`, `UseCase`), the Ktor `HttpClient` setup, and `multiplatform-settings`-backed token storage. |
| `shared/features/<name>/` | Per-feature business logic — Decompose `Component` plus its `ViewModel`/state/intent types. Currently `auth` and `dashboard`. |
| `shared/repositories/<name>/` | Repository interfaces + implementations consumed by feature modules. Currently `auth`. |
| `composeApp/` | Shared Compose Multiplatform UI host targeting Android, iOS, and JVM Desktop. Owns `App.kt`, the Decompose `RootComponent`, and platform entry points (`androidMain`, `iosMain`, `jvmMain`). |
| `composeApp/features/<name>/` | Per-feature Compose UI that renders the matching `shared/features/<name>` component. Currently `auth` and `dashboard`. |
| `design_system/` | Shared Compose Multiplatform design system library. Applies `crossplatform.kmp.library.compose`. |
| `server/` | JVM-only Ktor backend (Netty + Exposed/Postgres + JWT auth via Koin). Depends on `shared` for wire types and constants. |
| `iosApp/` | Swift/SwiftUI wrapper that embeds the Compose UI via `UIViewControllerRepresentable`. |

Navigation is **Decompose**: a `RootComponent` (in `composeApp/commonMain`) owns a `StackNavigation` and constructs feature components from `shared/features/*`. Feature UI in `composeApp/features/*` consumes the matching shared component — keep all stateful logic in `shared/features/*`, never in the Compose layer.

### Working principle

The owner is an Android engineer with limited iOS and backend experience. The
default for any new code is:

> **Maximise Kotlin sharing. `shared` and `commonMain` first; platform source
> sets only when a Kotlin API genuinely doesn't exist for what's needed.**

When in doubt about which module/source set something belongs in, ask. Don't
silently drop code into `androidMain` "for now."

### Module placement rules

- **Cross-cutting domain models, networking clients, shared storage** → `shared/`.
- **Repository interfaces & implementations** → `shared/repositories/<name>/`.
- **Per-feature Decompose `Component`s, `ViewModel`s, state/intent types** → `shared/features/<name>/`.
- **Per-feature Compose UI (screens, content composables)** → `composeApp/features/<name>/`.
- **App-level Compose UI (root navigation host, theming hookup)** → `composeApp/commonMain`.
- **Server-side routes, request/response handling, persistence** → `server`.
- **DTOs and constants used by both client and server** → `shared/commonMain`.
  Keeping the wire format in one place is the main reason `server` depends on
  `shared` — use it. When adding a new endpoint, define the request/response
  data classes in `shared` and reference them from both sides.

When adding a new feature, create both halves: `shared/features/<name>` (logic)
and `composeApp/features/<name>` (UI), and register the module in
`settings.gradle.kts`.

### Convention plugins (build-logic)

`build-logic/` is a composite build (wired via `pluginManagement { includeBuild("build-logic") }`). It exposes two plugins:

| Plugin ID | Class | What it does |
|-----------|-------|--------------|
| `crossplatform.kmp.library` | `KmpLibraryConventionPlugin` | Applies `com.android.library` + `kotlinMultiplatform`, registers `androidTarget`, `iosArm64`, `iosSimulatorArm64`, `jvm()` targets, sets `compileSdk`/`minSdk`/JVM 11 from the version catalog. |
| `crossplatform.kmp.library.compose` | `KmpLibraryComposeConventionPlugin` | Applies the base plugin above, then adds `org.jetbrains.compose` + `org.jetbrains.kotlin.plugin.compose` and wires standard Compose Multiplatform dependencies into `commonMain`. |

New KMP library modules should apply one of these instead of configuring targets and SDK versions by hand. The `build-logic/build.gradle.kts` compiles against three `compileOnly` entries in `libs.versions.toml` (`gradle-plugin-android`, `gradle-plugin-kotlin`, `gradle-plugin-compose-multiplatform`).

### Expect / Actual pattern

When platform-specific behaviour is needed (HTTP engine selection, secure
storage, etc.), declare an `expect` in `commonMain` and provide an `actual` in
each of `androidMain`, `iosMain`, and `jvmMain`. Prefer this over `if/when` on a
runtime OS check.

When adding an `expect` declaration, generate **all three** actuals in the same
change. Don't leave one as a TODO — Desktop and iOS get forgotten most often.

### Source set layout (composeApp)

```
composeApp/src/
  commonMain/   # shared Compose UI (App.kt is the root composable)
  androidMain/  # MainActivity
  iosMain/      # MainViewController (returns ComposeUIViewController)
  jvmMain/      # main() — creates a Compose application window
  commonTest/   # shared tests (kotlin.test + JUnit)
```

## Key Dependencies & Versions

Versions are centralised in `gradle/libs.versions.toml`. Always add new
dependencies to the version catalog, not directly in a module's build script.

- Kotlin **2.3.21**, Compose Multiplatform **1.10.3**
- Ktor **3.4.3** (Netty on server; OkHttp on Android/JVM, Darwin on iOS for the client)
- Material3 `1.10.0-alpha05`
- Decompose **3.5.0** for navigation/components
- Koin **4.2.1** for DI across `shared`, `composeApp`, and `server`
- `multiplatform-settings` **1.3.0** for client-side key/value storage (auth tokens)
- Server-side: Exposed **0.56.0** + HikariCP + PostgreSQL **42.7.4**, JWT via `ktor-server-auth-jwt`, password hashing via `at.favre.lib:bcrypt`
- Android minSdk **29**, targetSdk **36**, JVM target **11**
- Compose Hot Reload plugin (`org.jetbrains.compose.hot-reload 1.0.0`) is
  enabled for desktop development — prefer the Desktop target for fast UI
  iteration.

### Not yet wired up

- **Local persistence** (Room KMP / SQLDelight) is not set up. `multiplatform-settings`
  is currently used for small key/value data (e.g. auth tokens). When a real
  database is needed, confirm the choice before adding it.

## Code quality pipeline

Tools: **ktfmt** (formatter) + **detekt** (static analysis), both applied via the `crossplatform.code.quality` convention plugin.

```bash
./gradlew ktfmtFormat          # auto-format all Kotlin source (run locally before committing)
./gradlew ktfmtCheck           # verify formatting — fails on unformatted files (used in CI)
./gradlew detekt               # static analysis across all modules
./gradlew check                # runs ktfmtCheck + detekt + tests (full CI gate)
```

ktfmt uses **Google style**: 2-space indent, 4-space continuation indent, max line length 100.
Detekt config lives in `config/detekt/detekt.yml`. It builds on detekt defaults with a few project-specific overrides: `MagicNumber` disabled (too noisy in Compose), `formatting` rule set disabled (ktfmt owns that), and `LongMethod` threshold raised to 60.

### Which modules have code quality applied

| Module | How |
|---------------|----------------------------------------------------------------------|
| `design_system` | Inherited via `crossplatform.kmp.library` (→ `crossplatform.code.quality`) |
| `composeApp` | Explicit `id("crossplatform.code.quality")` |
| `shared` | Explicit `id("crossplatform.code.quality")` |
| `server` | Explicit `id("crossplatform.code.quality")` |

New modules using `crossplatform.kmp.library` or `crossplatform.kmp.library.compose` inherit it automatically.

## Formatting

Enforced by ktfmt (Kotlin) and `.editorconfig` (everything else):

| File type | Indent | Continuation indent |
|-----------|--------|---------------------|
| `*.kt`, `*.kts` | 2 spaces | 4 spaces |
| `*.xml`, `*.html` | 4 spaces | — |
| `*.json`, `*.yaml`, `*.toml` | 2 spaces | — |

All files: LF line endings, UTF-8, final newline, no trailing whitespace.

## Conventions

- **Async:** `suspend fun` for one-shots, `Flow` for observable streams. Don't
  expose `Deferred` or callbacks across module boundaries.
- **Errors:** prefer `Result<T>` or sealed error types over throwing across
  layer boundaries. The server should map exceptions to proper HTTP responses
  rather than letting them propagate.
- **Serialization:** kotlinx.serialization. Annotate shared DTOs with
  `@Serializable`.
- **Resources:** Compose resources live in
  `composeApp/src/commonMain/composeResources/`. Access via the generated
  `Res` class.
- **Lifecycle / ViewModel:** use the Jetbrains multiplatform lifecycle
  artifact (`androidx.lifecycle.viewmodel.compose.viewModel { ... }`) so the
  same code works on Android, iOS, and Desktop.
- **No `LocalContext.current`** in `composeApp/commonMain` — it's Android-only.
  If context-like behaviour is needed, design an `expect`/`actual` abstraction
  in `shared`.

## Workflow notes

- After changes to `shared` or `composeApp/commonMain`, the iOS framework
  needs relinking before Xcode picks them up. If the Xcode build is stale,
  run `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`.
- For UI iteration, use the **Desktop target** with hot reload — it's the
  fastest feedback loop and exercises the same `commonMain` Compose code as
  Android and iOS.
- The owner does not use Xcode day-to-day. Anything that requires editing
  `iosApp/` Swift code should be flagged for manual review.

## Things to avoid

- Don't suggest `kapt`. Use `ksp`.
- Don't add Java-only or Android-only libraries to `shared/commonMain` or
  `composeApp/commonMain`.
- Don't write SwiftUI code unless explicitly asked. The iOS shell exists; the
  goal is to keep it minimal.
- Don't duplicate request/response models between `server` and clients —
  define them once in `shared`.

## Notes

- Gradle configuration cache and build cache are both enabled
  (`gradle.properties`).
- The server shares `Constants.kt` (SERVER_PORT) with clients via the `shared`
  module — keep networking constants there.
