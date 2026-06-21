# CLAUDE.md

Guidance for Claude Code when working in this repo.

## Build & Run

```bash
# Android
./gradlew :composeApp:assembleDebug    # build debug APK
./gradlew :composeApp:installDebug     # install on connected device

# Server (Ktor) — port 8080, dev mode supplies a built-in JWT secret
docker compose up -d                   # start Postgres (credentials match AppConfig defaults)
./gradlew :server:run
./scripts/seed_db.sh                   # seed 5 users + 10 productions
docker compose stop                    # pause, data persists
docker compose down -v                 # wipe DB volume entirely

# Tests
./gradlew test                                                   # all modules
./gradlew :shared:test --tests "com.frame.zero.SharedCommonTest" # single class

# Screenshot tests (Roborazzi) — see "Screenshot tests" below
./gradlew :composeApp:shared:design_system:recordRoborazziDebug  # (re)generate goldens
./gradlew :composeApp:shared:design_system:verifyRoborazziDebug  # fail on visual drift
./gradlew :composeApp:shared:design_system:compareRoborazziDebug # diff only, don't fail

# Code quality
./gradlew ktlintFormat                 # auto-format
./gradlew check                        # ktlintCheck + detekt + tests
```

**iOS** — open `iosApp/iosApp.xcodeproj` in Xcode and run. Relink the framework
after `shared`/`composeApp/commonMain` changes:
`./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`.

## Architecture

Composite Gradle build + native iOS wrapper.

| Module | Purpose |
|--------|---------|
| `build-logic/` | Convention plugins (composite build). |
| `shared/` | Multiplatform business logic; no UI. `Constants`, the **client copy** of the wire DTOs (`dto/`, `auth/dto/`) and wire enums (`Genre`, `ProductionPhase`, `ProductionSort`, `ScheduleEventKind`), domain models (`User`, `DomainError`, `Outcome`, `UseCase`, `Production`, `Schedule`, …), DTO↔domain mappers, Ktor `HttpClient` (`core/network/`), session machine (`core/session/`), `multiplatform-settings` token storage. |
| `shared/features/<name>/` | Per-feature logic — Decompose `Component`, `ViewModel`, state/intent, feature-local Koin module. (`account`, `auth`, `home`, `production`, `production-details`, `task-details`.) |
| `shared/repositories/<name>/` | Repository interfaces + impls (`auth`, `user`, `dashboard`, `productions`, `schedule`, `tasks`). `productions` is offline-first — see [Offline-first repositories](#offline-first-repositories). |
| `shared/ui_text/` | Platform-agnostic `UiText` sealed type (`Dynamic` string / `Resource` + args) so shared ViewModels can carry strings without depending on Compose. No UI. |
| `shared/integrations/<name>/` | Optional backend integrations wired as self-registering plugins. `firebase` provides `FirebaseCrashlyticsLogSink`/`FirebaseAnalyticsSink` (GitLive SDK) — see [Self-registering plugins](#self-registering-plugins). |
| `composeApp/` | Compose Multiplatform UI host (Android, iOS). Owns `App.kt`, the Decompose `RootComponent`, platform entry points. |
| `composeApp/features/<name>/` | Per-feature Compose UI rendering the matching shared component. |
| `composeApp/shared/design_system/` | Design system library. Applies `crossplatform.library.compose`. |
| `composeApp/shared/ui_text/` | Compose-side resolver for `UiText` — `@Composable UiText.asString()` turns a `UiText.Resource` into a `stringResource(...)`. Pairs with `shared/ui_text`. |
| `server/` | JVM Ktor backend (Netty + Exposed/Postgres + JWT via Koin). Packages: `auth`, `dashboard`, `notification`, `production`, `schedule`, `task`, `domain`, `dto`, `common`/`config`. Schema is created by Flyway migrations on boot (Exposed `Table` defs are the read/write mapping). **Depends on no client module** — owns its own copy of the wire types (`com.frame.zero.dto.*`, `auth.dto.*`, the wire enums) under `server/src/main`, intentionally duplicated from `shared/`'s copy so the server can be lifted into a standalone repo. (`Constants`/`SERVER_PORT` is client-only — server hardcodes its bind port.) See [Wire contract duplication](#wire-contract-duplication). |
| `iosApp/` | Swift/SwiftUI wrapper. Minimal — flag any Swift edits for manual review. |

Navigation is **Decompose**. `RootComponent` (`composeApp/commonMain`) owns a
`StackNavigation`, constructs feature components from `shared/features/*`,
collects `SessionManager.state`, and swaps the whole stack on `SessionState`
(`Loading`→`Splash`, `LoggedOut`→`Auth`, `LoggedIn`→`Home`). Auth-gated screens
react to `SessionManager`, not constructor props.

### Decompose component conventions

- Constructor order: `ComponentContext` first, then nav callbacks, then ViewModel
  factories. Delegate `: ComponentContext by componentContext`.
- Sub-navigation: sibling sealed `Config` (stack) and `Child` (UI), declared
  inside the component class.
- ViewModels are Koin `factory { }`, injected via `(ComponentContext) -> Component`
  lambdas (or `(arg) -> ViewModel`) — keeps Koin out of `composeApp/commonMain`
  constructors.

### Domain ↔ DTO boundary

- DTOs in `shared/commonMain/com/frame/zero/dto/<feature>/`; domain types in
  `.../domain/<feature>/`. Map via sibling `*Mapper.kt`.
- UI hints (accent colors, badge labels) are extension functions on the domain
  enum, never DTO fields.

### Wire contract duplication

The wire DTOs are **deliberately duplicated**, not shared via a common module:

- **Client copy** lives in `shared/commonMain` (`com.frame.zero.dto.*`, `auth.dto.*`,
  the wire enums `Genre`/`ProductionPhase`/`ProductionSort`/`ScheduleEventKind`/`DevicePlatform`).
- **Server copy** lives in `server/src/main` under the **same package names**.

`Constants`/`SERVER_PORT` is **not** duplicated — it's client-only (`shared`'s
`NetworkConfig` builds the base URL from it); the server hardcodes its bind port.

Identical FQNs compiled from separate source = no build dependency either way; they
never share a classpath (server is a leaf), so the duplicated FQN is not a collision.
The price: the contract is **kept in sync by hand** — when you change a wire shape
(add/remove/rename a field, change an enum), **edit both copies** in the same change
or client and server silently drift.

### Offline-first repositories

Paginated lists use **Room (KMP) + Paging 3 `RemoteMediator`**.
`shared/repositories/productions` is the reference impl.

- Repo interface returns `Flow<PagingData<DomainType>>`. `*Impl` builds a `Pager`
  whose `pagingSourceFactory` is a Room DAO query and whose `remoteMediator`
  writes API pages into Room (REFRESH replaces, APPEND cursor-paginates). UI
  observes Room, never the network directly.
- Room layer lives in the same module under `local/` (`FrameZeroDatabase`,
  `*Entity`, `*Dao`, `*RemoteKeyEntity`, `*EntityMapper`). Apply `libs.plugins.ksp`
  + `libs.plugins.androidxRoom`, mark the db `@Database(exportSchema = false)`,
  point the Room Gradle plugin at `build/schemas` (it requires a `schemaDirectory`
  even when export is off), and register `ksp` configs for `kspAndroid`/`kspIosArm64`/
  `kspIosSimulatorArm64` (no `kspJvm` — no module targets the JVM). Not in production
  yet, so no schema JSONs or migrations — flip `exportSchema = true` and start
  versioning when that changes.
- Room's KMP builder is platform-specific. Each db-owning module exposes
  `interface DatabaseBuilderFactory` in `commonMain` with `Android*`/`Ios*` actuals,
  wired through Koin's `platformModule()`. The Koin module sets `BundledSQLiteDriver`
  and `Dispatchers.Default` query context.
- **Sign-out cleanup.** Any module owning local user-scoped state (Room DB, cached
  files, in-memory user data) implements `interface SessionCleaner { suspend fun clear() }`
  (`shared/.../core/session/SessionCleaner.kt`) and registers it via
  `single { … } bind SessionCleaner::class`. `SessionManager` injects them via
  Koin's `getAll()` and invokes each under its own `runCatching` during
  `forceLogout()` (covers both manual sign-out and refresh-rotation auto-logout via
  `LogoutSignal.emit()`). Tokens are cleared *after* cleaners run. Reference impl:
  `ProductionsSessionCleaner` → `ProductionsDao.clearAll()`.

### Self-registering plugins

Cross-cutting concerns are built as **self-registering plugins** so a backend can be
added or removed in one DI line, and so the pattern is reusable as a blueprint in other
projects. Each concern has three parts:

1. **Sink contract** — an interface implementations provide (`SessionCleaner`, `LogSink`,
   `AnalyticsSink`).
2. **Facade** — a `single` service the app injects and calls (`Logger`, `Analytics`; for
   cleanup the consumer is `SessionManager`). Its impl receives `getAll<Sink>()` and
   **fans out to every registered sink, each under its own `runCatching`** so one bad sink
   can't break the others.
3. **Registration** — `single { SomeSink(...) } bind XSink::class` in the concern's Koin
   module. **To add a new backend (a real log destination, Crashlytics, Firebase, …),
   implement the sink interface and add one `bind` line — nothing else changes.**

Instances: `SessionCleaner` (`shared/.../core/session/`), `LogSink` →
`Logger`/`loggingModule` (`shared/.../core/logging/`), `AnalyticsSink` →
`Analytics`/`analyticsModule` (`shared/.../core/analytics/`). The reference sink impls
live in `shared/integrations/firebase` (`FirebaseCrashlyticsLogSink`,
`FirebaseAnalyticsSink`) — opt-in: the module is only on the classpath when its Koin
module is registered, and its Android build needs user-supplied (gitignored) Firebase
config files. With no integration registered the facades fan out over an empty
`getAll()`, which is a no-op.

### Module placement

- Cross-cutting domain/network/storage → `shared/`.
- Repository interfaces + impls → `shared/repositories/<name>/`.
- Feature logic (`Component`, `ViewModel`, state/intent) → `shared/features/<name>/`.
- Feature UI → `composeApp/features/<name>/`.
- Root nav, theming hookup → `composeApp/commonMain`.
- Server routes/handlers/persistence → `server/`.
- DTOs & wire-facing enums → **two hand-synced copies**, one per side (client in
  `shared/commonMain`, server in `server/src/main`, same package names) — see
  [Wire contract duplication](#wire-contract-duplication).

New feature = both halves (`shared/features/<name>` + `composeApp/features/<name>`),
registered in `settings.gradle.kts`.

### Working principle

Owner is an Android engineer with limited iOS/backend experience. Default:
**maximise Kotlin sharing — `shared`/`commonMain` first; platform source sets only
when a Kotlin API genuinely doesn't exist**. When in doubt, ask. Don't silently
drop code into `androidMain` "for now".

### Convention plugins (build-logic)

- `crossplatform.library` — applies `com.android.library` + `kotlinMultiplatform`,
  registers `androidTarget`/`iosArm64`/`iosSimulatorArm64`, pulls SDK/JVM versions
  from the catalog, applies `crossplatform.code.quality`.
- `crossplatform.library.compose` — above + Compose Multiplatform +
  compose-compiler + standard Compose deps in `commonMain`.

New KMP library modules apply one of these instead of configuring targets by hand.

### Expect / Actual

For platform-specific behaviour (HTTP engine, secure storage, Room builder),
declare `expect` in `commonMain` and provide `androidMain` **and** `iosMain`
actuals in the same change. No module targets the JVM anymore (`shared` dropped its
`jvm()` target once `:server` was decoupled), so an `expect` needs exactly those two
actuals — no `jvmMain`. No TODO actuals. Don't `if/when` on runtime OS.

### Tests

`commonTest/` uses `kotlin.test`. ViewModel/repository tests use a per-feature
`testing/Fakes.kt` (see `shared/features/auth/.../testing/Fakes.kt`). No
mockito/mockk in shared code.

### Screenshot tests

Roborazzi golden coverage, piloted in `composeApp/shared/design_system`: `PreviewScreenshotTest`
auto-captures every `@LightDarkPreview`/`@Preview` (Light + Dark) — no per-component code. Goldens
are committed under `<module>/src/androidUnitTest/screenshots/`; `record` regenerates, `verify`
fails on drift (`./gradlew check` does **not** — run `verifyRoborazziDebug` explicitly).
Suite is exact-match: exclude flaky animated previews by `methodName` via the test's
`ANIMATED_PREVIEWS` set (e.g. `CtaButtonPreview` → Material3 `LoadingIndicator`).

## Key dependencies

All versions in `gradle/libs.versions.toml` — add new deps to the catalog, not
module scripts.

- Kotlin **2.4.0**, Compose Multiplatform **1.11.1**, AGP **9.2.1**, KSP **2.3.8**
- Ktor **3.5.0** (Netty server; OkHttp on Android, Darwin on iOS for the client)
- Material3 `1.10.0-alpha05`, Decompose **3.5.0**, Koin **4.2.1**
- `multiplatform-settings` **1.3.0**, `kotlinx-datetime` **0.8.0** (`LocalDate` in DTOs;
  timestamps are `kotlin.time.Instant` — never `java.time.*`)
- AndroidX Room **2.8.4** (KMP) + Paging **3.5.0** + SQLite bundled **2.6.2** — offline-first
- Server: Exposed **1.3.0** + HikariCP **7.0.2** + PostgreSQL **42.7.11** + Flyway (DDL),
  JWT via `ktor-server-auth-jwt`, bcrypt for passwords; tests use Testcontainers Postgres
- Tests: Robolectric **4.16.1** + `compose-uiTestJUnit4`; Roborazzi **1.63.0** +
  ComposablePreviewScanner **0.9.0** for screenshot goldens (see [Screenshot tests](#screenshot-tests))
- Android minSdk **29**, compileSdk/targetSdk **37**, JVM target **11**

### Dependency verification (supply-chain integrity)

`gradle/verification-metadata.xml` pins a **sha256 checksum for every artifact**
(jars, AARs, POMs, plugins) Gradle resolves. Its mere presence makes Gradle verify
every download on every build — a mismatched/tampered/swapped artifact **fails the
build**. This is tamper-evident resolution; don't hand-edit the file.

**You MUST regenerate it whenever you change a dependency or plugin version** (any
edit to `gradle/libs.versions.toml` or a `*.gradle.kts` dependency), or the build
fails with `Dependency verification failed` for the new/changed artifact. Run:

```bash
# Regenerate after any dependency/plugin change. Covers the full CI surface
# (all modules, both platforms, tests, release R8) so no artifact is missed.
# --write-verification-metadata is incompatible with the configuration cache.
# Needs Docker up (server Testcontainers) and resolves iOS klibs on macOS.
FRAMEZERO_API_BASE_URL=https://api.framezero.invalid \
  ./gradlew --write-verification-metadata sha256 --no-configuration-cache \
    check :composeApp:shared:design_system:verifyRoborazziDebug \
    :composeApp:assembleRelease
```

Then **review the `git diff`** of `verification-metadata.xml` — the added/changed
checksums should correspond only to the deps you bumped. Commit it with the version
change. (sha256 only — no PGP/keyring, to avoid keyserver flakiness.)

## Server config

`AppConfig.fromEnv()` reads env vars. Dev mode (`io.ktor.development=true`, set by
`:server:run`) supplies a hardcoded `JWT_SECRET`; production (`KTOR_ENV=production`)
fails fast without one. Other vars (`JWT_ISSUER`/`AUDIENCE`/`REALM`, `DATABASE_URL`/
`USER`/`PASSWORD`) default to match `docker-compose.yml` — see
`server/.../config/AppConfig.kt`. Access-token TTL 15 min, refresh 30 days, in
`JwtConfig`.

`FIREBASE_CREDENTIALS_PATH` (path to a Firebase service-account JSON) is **required at
boot in every mode** — `AppConfig.validate()` fails fast without it — because task
assignments send FCM pushes via the Firebase Admin SDK (`FirebaseAdminPushSender`). Get
the file from the Firebase console (Project settings → Service accounts). Tests don't
need it: they construct `AppConfig` directly and inject a fake `PushSender`.

```bash
# Local dev — DB defaults work out of the box; Firebase creds are required
export FIREBASE_CREDENTIALS_PATH=/absolute/path/to/service-account.json
docker compose up -d && ./gradlew :server:run
# Override defaults (e.g. staging DB): cp .env.example .env first

# Production-style boot
export KTOR_ENV=production
export JWT_SECRET=$(openssl rand -base64 32)
export DATABASE_URL=jdbc:postgresql://prod-host:5432/framezero
export DATABASE_USER=... DATABASE_PASSWORD=...
export FIREBASE_CREDENTIALS_PATH=/run/secrets/firebase-service-account.json
./gradlew :server:run
```

### Schema

**Flyway owns DDL.** `DatabaseFactory.init` runs `Flyway.configure()...migrate()` on boot
against the versioned SQL migrations in `server/src/main/resources/db/migration`
(`V1__baseline_schema.sql`, …). The Exposed `Table` defs are the read/write mapping only —
they no longer create the schema. Adding schema: add a new `V<n>__*.sql` migration **and**
update the matching Exposed `Table` in the same change (keep them in sync, like the wire
DTOs). Repo tests run against Testcontainers Postgres (`:server:test`, needs Docker), so the
migration is exercised on real Postgres.

## Client storage

- `multiplatform-settings` for small k/v (auth tokens, prefs).
- **Room (KMP)** for paginated/offline-first features — see [Offline-first
  repositories](#offline-first-repositories). Each db lives in its owning repo
  module. Don't add a second persistence library (SQLDelight, Realm, …) without
  confirming.

## Design system

All `composeApp` UI goes through `AppTheme`. Wrap the root with `AppTheme { ... }`
(handles dark/light via `isSystemInDarkTheme()`).

```kotlin
AppTheme.colorSystem.<token>      // background, textPrimary, accent, errorText, priorityHighSurface, …
AppTheme.typographySystem.<token> // titleSection, bodyStandard, bodySmall, label, button
AppTheme.spacingSystem.<token>    // xxs xs sm md lg xl xxl x3l x4l x5l x6l
AppTheme.radiusSystem.<token>     // xs sm segItem md lg input button card sheet circle
```

Full token list in `composeApp/shared/design_system/` (`ColorSystem.kt` /
`ThemeOptions.kt`) — read those to look up or add tokens.

Rules:
- Never use `MaterialTheme.colorScheme`/`typography`, hardcoded `Color(0xFF…)`, or
  raw `dp`/`sp` literals for visual tokens. Pick the closest semantic token; if none
  fits, add one to `ColorSystem` + `ThemeOptions`.
- **Design system locals:** when a sub-system (`colorSystem`, `spacingSystem`,
  `typographySystem`) is referenced more than once in a composable, extract it to a
  local val at the top (`val colorSystem = AppTheme.colorSystem`). Single-use can be
  inline.
- Use `spacingSystem` tokens only for spacing (padding, gaps, spacer sizes). Never
  for element sizes, widths, or border widths — use explicit `Dp` values hoisted to
  top-of-file `val`s.
- For gaps, use `VerticalSpacer`/`HorizontalSpacer(AppTheme.spacingSystem.spaceN)`
  from `com.frame.zero.shared.design_system.widgets`. Never a raw
  `Spacer(Modifier.height/width(...))`.
- For clickable surfaces, use `Modifier.clickableWithRipple(color = ...)` from
  `com.frame.zero.shared.design_system.modifier` instead of bare `Modifier.clickable`.
  It wires a themed Material 3 ripple (`bounded`/`radius` configurable).
- Annotate previews with `@LightDarkPreview` (from
  `com.frame.zero.shared.design_system`), not plain `@Preview` — generates both
  variants.
- **Components directory:** composables extracted from a screen live in a
  `components/` subdir next to the parent screen (e.g. `signin/components/SignInHeader.kt`).
  Composables shared across screens in a feature go in feature-level `ui/components/`
  (e.g. `ui/components/AuthLogoHeader.kt`). Each file defines one primary `internal`
  composable + a `@LightDarkPreview`.
- Always give new composables a default `Modifier` param. Hoist magic numbers
  (sizes, borders) to top-of-file `val`s.
- Always use `collectAsStateWithLifecycle()`, not `collectAsState()`.

## Conventions

- **Readability:** descriptive names, small single-purpose functions, straightforward
  control flow over clever one-liners.
- **Async:** `suspend fun` for one-shots, `Flow` for streams. Don't expose `Deferred`
  or callbacks across module boundaries.
- **Errors:** canonical result is `Outcome<T>` (sealed, `shared/.../domain/Outcome.kt`)
  across layer boundaries. Reserve `Result<T>` for ad-hoc internal flows. Never throw
  across modules. Server maps exceptions to HTTP via `AppException`.
- **StateFlow updates:** always `_state.update { it.copy(...) }`, never
  `_state.value = _state.value.copy(...)` — `update` is atomic.
- **Serialization:** kotlinx.serialization. `@Serializable` on DTOs.
- **Wire format excludes UI hints:** DTOs carry only semantic fields (`phase`,
  `status`, enums) — no `*Color`, `*Hint`, badge labels, or cosmetic fields. Derive
  client-side in mappers (e.g. `ProductionPhase.toAccentColorHint()`).
- **Presentation logic lives in the ViewModel:** date/time/number formatting,
  label/initials/display-string building, and choosing display text belong in the
  ViewModel (as `private` members), never in `*Ui.kt`/`*Mapper.kt`. Mapper extensions
  (`toUi`, `toState`) may only do structural field/enum mapping. The ViewModel owns
  the domain/DTO→UI-model mapping — see `ScheduleTabViewModel` for the reference shape.
- **DI:** each feature exposes `val <name>Module = module { ... }`, registered in
  `composeApp/.../di/AppModule.kt`. ViewModels `factory`, repositories `single`.
  Don't reference Koin from `composeApp/commonMain` constructors — inject component
  factories.
- **Interface + Impl naming:** single-impl interfaces use `<Interface>Impl` in a
  same-named file, side-by-side in the same package (`ProductionsRepository` →
  `ProductionsRepositoryImpl`). No technology/strategy suffixes (`*Exposed`, `Ktor*`,
  `OfflineFirst*`). **Exception:** platform actuals in `androidMain`/`iosMain` keep the
  platform prefix (`AndroidDatabaseBuilderFactory`) to avoid DI/stack-trace collisions.
- **HTTP engine** via `expect/actual` in `shared/.../core/network/HttpClientFactory.kt`
  — OkHttp on Android, Darwin on iOS.
- **Resources:** Compose resources in `composeApp/src/commonMain/composeResources/`,
  via generated `Res`.
- **Strings from shared code:** a shared ViewModel can't call `stringResource` (no
  Compose), so it emits `UiText` (`shared/ui_text`) — `UiText.Dynamic(text)` for an
  already-resolved string, `someRes.asUiText(args…)` for a resource reference. The UI
  resolves it with `UiText.asString()` (`composeApp/shared/ui_text`) at render time.
  Never resolve resources into raw `String`s inside `shared`.
- **Lifecycle/ViewModel:** shared ViewModels implement Essenty
  `InstanceKeeper.Instance` (manual `CoroutineScope` cancelled in `onDestroy()`); the
  owning Decompose `Component` creates one via `instanceKeeper.getOrCreate { factory() }`
  so it survives config changes. Not the `androidx.lifecycle … viewModel { }` helper.
- **No `LocalContext.current`** in `composeApp/commonMain` (Android-only) — design an
  `expect`/`actual` in `shared` instead.

## Things to avoid
- `java.time.*` in shared code — use `kotlinx-datetime`.
- Exposing Compose UI types from `shared/` — it's pure logic so iOS/server can consume it.
- SwiftUI changes unless explicitly asked.
- Domain logic on a DTO — map DTO→domain in a `*Mapper.kt`; keep the DTO a dumb wire shape.
