# CLAUDE.md

Guidance for Claude Code when working in this repo.

## Build & Run

```bash
# Android
./gradlew :composeApp:assembleDebug    # build debug APK
./gradlew :composeApp:installDebug     # install on connected device

# Server (Ktor) — port 8080, dev mode supplies a built-in JWT secret
./gradlew :server:run
./scripts/seed_db.sh                   # seed 5 users + 10 productions

# Tests
./gradlew test                                                   # all modules
./gradlew :shared:test --tests "com.frame.zero.SharedCommonTest" # single class

# Code quality
./gradlew ktlintFormat                 # auto-format
./gradlew check                        # ktlintCheck + detekt + tests
```

**iOS** — open `iosApp/iosApp.xcodeproj` in Xcode and run. To relink the
framework after `shared`/`composeApp/commonMain` changes:
`./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`.

## Architecture

Composite Gradle build + native iOS wrapper.

| Module | Purpose |
|--------|---------|
| `build-logic/` | Convention plugins (composite build). |
| `shared/` | Multiplatform business logic; no UI. Holds `Constants`, domain models (`User`, `DomainError`, `Outcome`, `UseCase`), DTOs, DTO↔domain mappers, Ktor `HttpClient` (`core/network/`), session machine (`core/session/`), and `multiplatform-settings` token storage. |
| `shared/features/<name>/` | Per-feature logic — Decompose `Component`, `ViewModel`, state/intent, feature-local Koin module. Currently `account`, `auth`, `home`, `production`, `production-details`, `task-details`. |
| `shared/repositories/<name>/` | Repository interfaces + impls. Currently `auth`, `user`, `dashboard`, `productions`, `schedule`. `productions` is offline-first (Room + Paging 3 `RemoteMediator`); see [Offline-first repositories](#offline-first-repositories). |
| `composeApp/` | Compose Multiplatform UI host (Android, iOS). Owns `App.kt`, the Decompose `RootComponent`, and platform entry points. |
| `composeApp/features/<name>/` | Per-feature Compose UI rendering the matching shared component. Same feature list as above. |
| `composeApp/shared/design_system/` | Shared design system library. Applies `crossplatform.kmp.library.compose`. |
| `server/` | JVM Ktor backend (Netty + Exposed/Postgres + JWT via Koin). Feature packages: `auth`, `dashboard`, `notification`, `production`, `schedule`, `task`, plus `common`/`config`. Schema is created from the Exposed `Table` definitions on boot. Depends on `shared` for wire types. |
| `iosApp/` | Swift/SwiftUI wrapper. Minimal — flag any Swift edits for manual review. |

Navigation is **Decompose**. `RootComponent` (in `composeApp/commonMain`)
owns a `StackNavigation`, constructs feature components from
`shared/features/*`, collects `SessionManager.state` and swaps the entire
stack on `SessionState` (`Loading`→`Splash`, `LoggedOut`→`Auth`,
`LoggedIn`→`Home`). Auth-gated screens react to `SessionManager`, not via
constructor props.

### Decompose component conventions

- Constructor order: `ComponentContext` first, then nav callbacks, then
  ViewModel factories. Delegate `: ComponentContext by componentContext`.
- Sub-navigation: sibling sealed `Config` (in the stack) and `Child` (in
  the UI), declared inside the component class.
- ViewModels are Koin `factory { }`, injected via
  `(ComponentContext) -> Component` lambdas (or `(arg) -> ViewModel`).
  Keeps Koin out of `composeApp/commonMain` constructors.

### Domain ↔ DTO boundary

- DTOs live in `shared/commonMain/com/frame/zero/dto/<feature>/`.
- Domain types in `shared/commonMain/com/frame/zero/domain/<feature>/`.
- Mapping via sibling `*Mapper.kt`. UI hints (accent colors, badge labels)
  are extension functions on the domain enum, never fields on the DTO.

### Offline-first repositories

Paginated lists use **Room (KMP) + Paging 3 `RemoteMediator`**.
`shared/repositories/productions` is the reference impl.

- Repo interface returns `Flow<PagingData<DomainType>>`. `*Impl` builds a
  `Pager` whose `pagingSourceFactory` is a Room DAO query and whose
  `remoteMediator` writes API pages into Room (REFRESH replaces, APPEND
  cursor-paginates). UI observes Room, never the network directly.
- Room layer lives in the same module under `local/` (`FrameZeroDatabase`,
  `*Entity`, `*Dao`, `*RemoteKeyEntity`, `*EntityMapper`).
  Apply `libs.plugins.ksp` + `libs.plugins.androidxRoom`, mark the
  database `@Database(exportSchema = false)`, point the Room Gradle
  plugin at `build/schemas` (it requires a `schemaDirectory` even when
  schema export is off), and register `ksp` configs for
  `kspAndroid`/`kspIosArm64`/`kspIosSimulatorArm64`/`kspJvm`. The app
  isn't in production yet, so we don't track schema JSONs or write
  migrations — flip `exportSchema = true` and start versioning when that
  changes.
- Room's KMP builder is platform-specific. Each db-owning module exposes
  `interface DatabaseBuilderFactory` in `commonMain` with `Android*`/
  `Ios*`/`Jvm*` actuals, wired through Koin's `platformModule()`. The
  Koin module sets `BundledSQLiteDriver` and `Dispatchers.Default` query
  context.
- **Sign-out cleanup.** Any module that owns local user-scoped state
  (Room DB, cached files, in-memory user data) implements
  `interface SessionCleaner { suspend fun clear() }`
  (`shared/.../core/session/SessionCleaner.kt`) and registers it in its
  Koin module via `single { … } bind SessionCleaner::class`.
  `SessionManager` injects the list via Koin's `getAll()` and invokes
  every cleaner — each under its own `runCatching`, so a failure in one
  doesn't strand the other state — during `forceLogout()`. This covers
  both user-initiated sign-out and the auto-logout path that fires when
  refresh-token rotation fails (`LogoutSignal.emit()`). Tokens are
  cleared *after* cleaners run so no future request can succeed against
  a half-cleared cache. Reference impl:
  `ProductionsSessionCleaner` → `ProductionsDao.clearAll()`.

### Module placement

- Cross-cutting domain/network/storage → `shared/`.
- Repository interfaces + impls → `shared/repositories/<name>/`.
- Feature logic (`Component`, `ViewModel`, state/intent) → `shared/features/<name>/`.
- Feature UI → `composeApp/features/<name>/`.
- Root nav, theming hookup → `composeApp/commonMain`.
- Server routes/handlers/persistence → `server/`.
- DTOs & shared constants → `shared/commonMain` — define once, reference from both sides.

New feature = both halves (`shared/features/<name>` + `composeApp/features/<name>`),
registered in `settings.gradle.kts`.

### Working principle

Owner is an Android engineer with limited iOS/backend experience. Default:
**maximise Kotlin sharing — `shared`/`commonMain` first; platform source sets
only when a Kotlin API genuinely doesn't exist**. When in doubt, ask. Don't
silently drop code into `androidMain` "for now".

### Convention plugins (build-logic)

- `crossplatform.kmp.library` — applies `com.android.library` +
  `kotlinMultiplatform`, registers `androidTarget`, `iosArm64`,
  `iosSimulatorArm64`, pulls SDK/JVM versions from the catalog,
  applies `crossplatform.code.quality`.
- `crossplatform.kmp.library.compose` — above + Compose Multiplatform +
  compose-compiler + standard Compose deps in `commonMain`.

New KMP library modules apply one of these instead of configuring targets
by hand.

### Expect / Actual

For platform-specific behaviour (HTTP engine, secure storage, Room
builder), declare `expect` in `commonMain` and provide actuals in
`androidMain` **and** `iosMain` in the same change. The `shared` module
also retains a `jvm()` target (needed by the `:server` module), so any
`expect` declared there must also have a `jvmMain` actual. Don't leave
any as TODO. Don't `if/when` on runtime OS.

### Tests

`commonTest/` uses `kotlin.test`. ViewModel/repository tests use a
per-feature `testing/Fakes.kt` (see `shared/features/auth/.../testing/Fakes.kt`).
Don't pull mockito/mockk into shared code.

## Key dependencies

All versions in `gradle/libs.versions.toml` — add new deps to the catalog,
not directly to module scripts.

- Kotlin **2.3.21**, Compose Multiplatform **1.10.3**, AGP **8.11.2**, KSP **2.3.8**
- Ktor **3.4.3** (Netty server; OkHttp on Android, Darwin on iOS for the client)
- Material3 `1.10.0-alpha05`, Decompose **3.5.0**, Koin **4.2.1**
- `multiplatform-settings` **1.3.0** (k/v storage)
- `kotlinx-datetime` **0.7.1** (`Instant`/`LocalDate` in DTOs — never `java.time.*`)
- AndroidX Room **2.8.4** (KMP) + Paging **3.5.0** + SQLite bundled **2.6.2** — offline-first repos
- Server: Exposed **1.2.0** + HikariCP **7.0.2** + PostgreSQL **42.7.11**, H2 **2.4.240** (tests), JWT via `ktor-server-auth-jwt`, bcrypt for passwords
- Android minSdk **29**, targetSdk **36**, JVM target **11**

## Server config

`AppConfig.fromEnv()` reads env vars. Dev mode (`io.ktor.development=true`,
set by `:server:run`) supplies a hardcoded `JWT_SECRET`; production mode
(`KTOR_ENV=production`) fails fast without one. Other env vars
(`JWT_ISSUER`/`AUDIENCE`/`REALM`, `DATABASE_URL`/`USER`/`PASSWORD`) have
sensible defaults — see `server/.../config/AppConfig.kt`. Access-token TTL
15 min, refresh 30 days, in `JwtConfig`.

Production-style boot:
```bash
export KTOR_ENV=production
export JWT_SECRET=$(openssl rand -base64 32)
export DATABASE_URL=jdbc:postgresql://prod-host:5432/framezero
./gradlew :server:run
```

### Schema

`DatabaseFactory.init` runs `SchemaUtils.create(...)` over the Exposed
`Table` definitions on boot (`CREATE TABLE IF NOT EXISTS`), so the schema —
including the declared indexes — is created from Kotlin, not SQL migrations.
Adding schema: edit the Exposed `Table` (columns + any `index(...)` in an
`init` block), verify on H2 PostgreSQL-mode via `:server:test`. `create`
won't alter existing columns, so on a schema change drop/recreate the dev
database.

## Client storage

- `multiplatform-settings` for small k/v (auth tokens, prefs).
- **Room (KMP)** for paginated/offline-first features — see
  [Offline-first repositories](#offline-first-repositories). Each db lives
  in the repository module that owns it. Don't add a second persistence
  library (SQLDelight, Realm, …) without confirming.

## Design system

All `composeApp` UI must go through `AppTheme`. Wrap the root with
`AppTheme { ... }` (handles dark/light via `isSystemInDarkTheme()`).

```kotlin
AppTheme.colorSystem.<token>      // background, textPrimary, accent, errorText, priorityHighSurface, …
AppTheme.typographySystem.<token> // titleSection, bodyStandard, bodySmall, label, button
AppTheme.spacingSystem.<token>    // xxs xs sm md lg xl xxl x3l x4l x5l x6l
AppTheme.radiusSystem.<token>     // xs sm segItem md lg input button card sheet circle
```

Full token list in `composeApp/shared/design_system/` (`ColorSystem.kt` /
`ThemeOptions.kt`) — read those to look up or add tokens.

Rules:
- Never use `MaterialTheme.colorScheme`/`typography`, hardcoded
  `Color(0xFF…)`, or raw `dp`/`sp` literals for visual tokens. Pick the
  closest semantic token; if none fits, add one to `ColorSystem` +
  `ThemeOptions`.
- **Design system locals:** When a design system sub-system (`colorSystem`,
  `spacingSystem`, `typographySystem`) is referenced more than once inside a
  composable, extract it to a local val at the top of the composable body
  (e.g. `val colorSystem = AppTheme.colorSystem`). Single-use references can
  be accessed inline.
- Use `spacingSystem` tokens only for spacing (padding, gaps, spacer sizes).
  Never use them for element sizes, widths, or border widths — use explicit
  `Dp` values hoisted to top-of-file `val`s for those.
- For vertical/horizontal gaps, use `VerticalSpacer(AppTheme.spacingSystem.spaceN)`
  and `HorizontalSpacer(AppTheme.spacingSystem.spaceN)` from
  `com.frame.zero.shared.design_system.widgets`. Never use a raw
  `Spacer(Modifier.height/width(...))`.
- For clickable surfaces, use `Modifier.clickableWithRipple(color = ...)`
  from `com.frame.zero.shared.design_system.modifier` instead of the bare
  `Modifier.clickable { ... }`. It wires a themed Material 3 ripple
  (`bounded`/`radius` configurable) so click feedback stays consistent
  across the app.
- Always annotate previews with `@LightDarkPreview` (from
  `com.frame.zero.shared.design_system`) instead of plain `@Preview`.
  This generates both light and dark variants automatically.
- **Components directory:** composables extracted from a screen live in a
  `components/` subdirectory next to their parent screen file (e.g.
  `signin/components/SignInHeader.kt` alongside `signin/SignInContent.kt`).
  Composables shared across multiple screens within the same feature go in
  the feature-level `ui/components/` package (e.g.
  `ui/components/AuthLogoHeader.kt`). Each component file defines one
  primary `internal` composable and includes a `@LightDarkPreview`.
- Always have a default `Modifier` parameter on new composables.
  Hoist magic numbers (sizes, borders) to top-of-file `val`s.
- Always use `collectAsStateWithLifecycle()` instead of `collectAsState()` in
  composables. It stops collection when the UI is not visible, saving CPU/battery.

## Conventions

- **Readability:** descriptive names, small single-purpose functions,
  straightforward control flow over clever one-liners.
- **Async:** `suspend fun` for one-shots, `Flow` for streams. Don't
  expose `Deferred` or callbacks across module boundaries.
- **Errors:** canonical result is `Outcome<T>` (sealed, in
  `shared/.../domain/Outcome.kt`) across layer boundaries. Reserve
  `Result<T>` for ad-hoc internal flows. Never throw across modules.
  Server maps exceptions to HTTP via `AppException`.
- **StateFlow updates:** Always use `_state.update { it.copy(...) }` instead
  of `_state.value = _state.value.copy(...)`. The `update` function is
  atomic and avoids race conditions with concurrent emissions.
- **Serialization:** kotlinx.serialization. `@Serializable` on DTOs.
- **Wire format excludes UI hints:** DTOs carry only semantic fields
  (`phase`, `status`, enums). No `*Color`, `*Hint`, badge labels, or
  cosmetic fields. Derive client-side in mappers
  (e.g. `ProductionPhase.toAccentColorHint()`).
- **DI:** Each feature exposes `val <name>Module = module { ... }`,
  registered in `composeApp/.../di/AppModule.kt`. ViewModels `factory`,
  repositories `single`. Don't reference Koin from `composeApp/commonMain`
  constructors — inject component factories.
- **Interface + Impl naming:** single-impl interfaces use `<Interface>Impl`
  in a same-named file, side-by-side in the same package
  (`ProductionsRepository` → `ProductionsRepositoryImpl`). Don't invent
  technology/strategy suffixes (`*Exposed`, `Ktor*`, `OfflineFirst*`).
  **Exception:** platform actuals in `androidMain`/`iosMain` (and
  `shared`'s `jvmMain`, retained for the `:server` consumer) keep the
  platform prefix (`AndroidDatabaseBuilderFactory`) so the classes don't
  collide in DI logs and stack traces.
- **HTTP engine** chosen via `expect/actual` in
  `shared/.../core/network/HttpClientFactory.kt` — OkHttp on Android,
  Darwin on iOS.
- **Resources:** Compose resources in
  `composeApp/src/commonMain/composeResources/`, via generated `Res`.
- **Lifecycle/ViewModel:** JetBrains multiplatform lifecycle
  (`androidx.lifecycle.viewmodel.compose.viewModel { ... }`).
- **No `LocalContext.current`** in `composeApp/commonMain` — Android-only;
  design an `expect`/`actual` in `shared` instead.

## Things to avoid

- `kapt` — use `ksp`.
- Java-only / Android-only libs in `shared/commonMain` or `composeApp/commonMain`.
- `java.time.*` in shared code — use `kotlinx-datetime`.
- Exposing Compose UI types from `shared/` — it's pure logic so iOS/server can consume it.
- SwiftUI changes unless explicitly asked.
- Duplicating request/response models between `server` and clients — define once in `shared`.
- Domain logic on a DTO — map DTO→domain in a `*Mapper.kt`; keep the DTO a dumb wire shape.
