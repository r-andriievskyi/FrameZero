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
| `shared/` | Multiplatform business logic; no UI. `Constants`, domain models (`User`, `DomainError`, `Outcome`, `UseCase`), DTOs, DTO↔domain mappers, Ktor `HttpClient` (`core/network/`), session machine (`core/session/`), `multiplatform-settings` token storage. |
| `shared/features/<name>/` | Per-feature logic — Decompose `Component`, `ViewModel`, state/intent, feature-local Koin module. (`account`, `auth`, `home`, `production`, `production-details`, `task-details`.) |
| `shared/repositories/<name>/` | Repository interfaces + impls (`auth`, `user`, `dashboard`, `productions`, `schedule`). `productions` is offline-first — see [Offline-first repositories](#offline-first-repositories). |
| `composeApp/` | Compose Multiplatform UI host (Android, iOS). Owns `App.kt`, the Decompose `RootComponent`, platform entry points. |
| `composeApp/features/<name>/` | Per-feature Compose UI rendering the matching shared component. |
| `composeApp/shared/design_system/` | Design system library. Applies `crossplatform.kmp.library.compose`. |
| `server/` | JVM Ktor backend (Netty + Exposed/Postgres + JWT via Koin). Packages: `auth`, `dashboard`, `notification`, `production`, `schedule`, `task`, `common`/`config`. Schema created from Exposed `Table` defs on boot. Depends on `shared` for wire types. |
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
  `kspIosSimulatorArm64`/`kspJvm`. Not in production yet, so no schema JSONs or
  migrations — flip `exportSchema = true` and start versioning when that changes.
- Room's KMP builder is platform-specific. Each db-owning module exposes
  `interface DatabaseBuilderFactory` in `commonMain` with `Android*`/`Ios*`/`Jvm*`
  actuals, wired through Koin's `platformModule()`. The Koin module sets
  `BundledSQLiteDriver` and `Dispatchers.Default` query context.
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
`Analytics`/`analyticsModule` (`shared/.../core/analytics/`). Logging and analytics
currently ship **no sinks** — the facades fan out over whatever `getAll()` returns, and
an empty list is a no-op, so real sinks and call-site consumption are a deliberate later
step. Add a backend by implementing the sink interface and adding one `bind` line.

### Module placement

- Cross-cutting domain/network/storage → `shared/`.
- Repository interfaces + impls → `shared/repositories/<name>/`.
- Feature logic (`Component`, `ViewModel`, state/intent) → `shared/features/<name>/`.
- Feature UI → `composeApp/features/<name>/`.
- Root nav, theming hookup → `composeApp/commonMain`.
- Server routes/handlers/persistence → `server/`.
- DTOs & shared constants → `shared/commonMain` — define once, reference both sides.

New feature = both halves (`shared/features/<name>` + `composeApp/features/<name>`),
registered in `settings.gradle.kts`.

### Working principle

Owner is an Android engineer with limited iOS/backend experience. Default:
**maximise Kotlin sharing — `shared`/`commonMain` first; platform source sets only
when a Kotlin API genuinely doesn't exist**. When in doubt, ask. Don't silently
drop code into `androidMain` "for now".

### Convention plugins (build-logic)

- `crossplatform.kmp.library` — applies `com.android.library` + `kotlinMultiplatform`,
  registers `androidTarget`/`iosArm64`/`iosSimulatorArm64`, pulls SDK/JVM versions
  from the catalog, applies `crossplatform.code.quality`.
- `crossplatform.kmp.library.compose` — above + Compose Multiplatform +
  compose-compiler + standard Compose deps in `commonMain`.

New KMP library modules apply one of these instead of configuring targets by hand.

### Expect / Actual

For platform-specific behaviour (HTTP engine, secure storage, Room builder),
declare `expect` in `commonMain` and provide `androidMain` **and** `iosMain`
actuals in the same change. `shared` also retains a `jvm()` target (for `:server`),
so any `expect` there also needs a `jvmMain` actual. No TODO actuals. Don't
`if/when` on runtime OS.

### Tests

`commonTest/` uses `kotlin.test`. ViewModel/repository tests use a per-feature
`testing/Fakes.kt` (see `shared/features/auth/.../testing/Fakes.kt`). No
mockito/mockk in shared code.

## Key dependencies

All versions in `gradle/libs.versions.toml` — add new deps to the catalog, not
module scripts.

- Kotlin **2.3.21**, Compose Multiplatform **1.10.3**, AGP **8.11.2**, KSP **2.3.8**
- Ktor **3.4.3** (Netty server; OkHttp on Android, Darwin on iOS for the client)
- Material3 `1.10.0-alpha05`, Decompose **3.5.0**, Koin **4.2.1**
- `multiplatform-settings` **1.3.0**, `kotlinx-datetime` **0.7.1** (`Instant`/`LocalDate`
  in DTOs — never `java.time.*`)
- AndroidX Room **2.8.4** (KMP) + Paging **3.5.0** + SQLite bundled **2.6.2** — offline-first
- Server: Exposed **1.2.0** + HikariCP **7.0.2** + PostgreSQL **42.7.11**, H2 **2.4.240**
  (tests), JWT via `ktor-server-auth-jwt`, bcrypt for passwords
- Android minSdk **29**, targetSdk **36**, JVM target **11**

## Server config

`AppConfig.fromEnv()` reads env vars. Dev mode (`io.ktor.development=true`, set by
`:server:run`) supplies a hardcoded `JWT_SECRET`; production (`KTOR_ENV=production`)
fails fast without one. Other vars (`JWT_ISSUER`/`AUDIENCE`/`REALM`, `DATABASE_URL`/
`USER`/`PASSWORD`) default to match `docker-compose.yml` — see
`server/.../config/AppConfig.kt`. Access-token TTL 15 min, refresh 30 days, in
`JwtConfig`.

```bash
# Local dev — defaults work out of the box
docker compose up -d && ./gradlew :server:run
# Override defaults (e.g. staging DB): cp .env.example .env first

# Production-style boot
export KTOR_ENV=production
export JWT_SECRET=$(openssl rand -base64 32)
export DATABASE_URL=jdbc:postgresql://prod-host:5432/framezero
export DATABASE_USER=... DATABASE_PASSWORD=...
./gradlew :server:run
```

### Schema

`DatabaseFactory.init` runs `SchemaUtils.create(...)` over the Exposed `Table`
defs on boot (`CREATE TABLE IF NOT EXISTS`) — schema (incl. declared indexes) comes
from Kotlin, not SQL migrations. Adding schema: edit the `Table` (columns + any
`index(...)` in an `init` block), verify on H2 PostgreSQL-mode via `:server:test`.
`create` won't alter existing columns, so drop/recreate the dev DB on a schema change.

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
  `OfflineFirst*`). **Exception:** platform actuals in `androidMain`/`iosMain` (and
  `shared`'s `jvmMain`) keep the platform prefix (`AndroidDatabaseBuilderFactory`) to
  avoid DI/stack-trace collisions.
- **HTTP engine** via `expect/actual` in `shared/.../core/network/HttpClientFactory.kt`
  — OkHttp on Android, Darwin on iOS.
- **Resources:** Compose resources in `composeApp/src/commonMain/composeResources/`,
  via generated `Res`.
- **Lifecycle/ViewModel:** JetBrains multiplatform lifecycle
  (`androidx.lifecycle.viewmodel.compose.viewModel { ... }`).
- **No `LocalContext.current`** in `composeApp/commonMain` (Android-only) — design an
  `expect`/`actual` in `shared` instead.

## Things to avoid

- `kapt` — use `ksp`.
- Java-only / Android-only libs in `shared/commonMain` or `composeApp/commonMain`.
- `java.time.*` in shared code — use `kotlinx-datetime`.
- Exposing Compose UI types from `shared/` — it's pure logic so iOS/server can consume it.
- SwiftUI changes unless explicitly asked.
- Duplicating request/response models between `server` and clients — define once in `shared`.
- Domain logic on a DTO — map DTO→domain in a `*Mapper.kt`; keep the DTO a dumb wire shape.
