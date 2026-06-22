# CLAUDE.md

Guidance for Claude Code when working in this repo. Composite Gradle build + native iOS wrapper.

## Build & Run

```bash
# Android
./gradlew :composeApp:assembleDebug    # build debug APK
./gradlew :composeApp:installDebug     # install on device

# Server (Ktor, port 8080; dev mode supplies a built-in JWT secret)
docker compose up -d                   # Postgres (creds match AppConfig defaults)
./gradlew :server:run
./scripts/seed_db.sh                   # seed 5 users + 10 productions
docker compose down -v                 # wipe DB volume

# Tests
./gradlew test                                                   # all modules
./gradlew :shared:test --tests "com.frame.zero.SharedCommonTest" # single class

# Screenshot tests (Roborazzi) — `check` does NOT run verify; run it explicitly
./gradlew :composeApp:shared:design_system:recordRoborazziDebug  # (re)generate goldens
./gradlew :composeApp:shared:design_system:verifyRoborazziDebug  # fail on drift

# Code quality
./gradlew ktlintFormat                 # auto-format
./gradlew check                        # ktlint + detekt + tests
```

**iOS** — open `iosApp/iosApp.xcodeproj` in Xcode and run. Relink after
`shared`/`composeApp/commonMain` changes:
`./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`. Flag any Swift edits for
manual review; don't touch SwiftUI unless asked.

## Architecture

| Module | Purpose |
|--------|---------|
| `build-logic/` | Convention plugins (composite build). |
| `shared/` | Multiplatform logic, no UI. `Constants`, client copy of wire DTOs (`dto/`, `auth/dto/`) + wire enums, domain models, DTO↔domain mappers, Ktor `HttpClient` (`core/network/`), session machine (`core/session/`), `multiplatform-settings` token storage. |
| `shared/features/<name>/` | Per-feature logic — Decompose `Component`, `ViewModel`, state/intent, Koin module. (`account`, `auth`, `home`, `production`, `production-details`, `task-details`.) |
| `shared/repositories/<name>/` | Repository interfaces + impls (`auth`, `user`, `dashboard`, `productions`, `schedule`, `tasks`). `productions` is offline-first. |
| `shared/ui_text/` | `UiText` sealed type (`Dynamic` / `Resource` + args) so shared ViewModels carry strings without Compose. |
| `shared/integrations/<name>/` | Optional backend integrations as self-registering plugins. `firebase` = Crashlytics/Analytics sinks (GitLive). |
| `composeApp/` | Compose Multiplatform UI host (Android, iOS). Owns `App.kt`, Decompose `RootComponent`, platform entry points. |
| `composeApp/features/<name>/` | Per-feature Compose UI for the matching shared component. |
| `composeApp/shared/design_system/` | Design system. Applies `crossplatform.library.compose`. |
| `composeApp/shared/ui_text/` | Compose resolver: `@Composable UiText.asString()` → `stringResource`. |
| `server/` | JVM Ktor (Netty + Exposed/Postgres + JWT via Koin). Packages: `auth`, `dashboard`, `notification`, `production`, `schedule`, `task`, `domain`, `dto`, `common`/`config`. Depends on no client module — owns its own copy of the wire types. |
| `iosApp/` | Swift/SwiftUI wrapper. Minimal. |

**Navigation = Decompose.** `RootComponent` (`composeApp/commonMain`) owns a
`StackNavigation`, builds components from `shared/features/*`, and swaps the whole stack on
`SessionState` (`Loading`→`Splash`, `LoggedOut`→`Auth`, `LoggedIn`→`Home`). Auth-gated
screens react to `SessionManager`, not constructor props.

### Decompose conventions

- Constructor order: `ComponentContext` first, then nav callbacks, then ViewModel factories.
  Delegate `: ComponentContext by componentContext`.
- Sub-nav: sibling sealed `Config` (stack) + `Child` (UI) inside the component class.
- ViewModels are Koin `factory { }`, injected via `(ComponentContext) -> Component` lambdas —
  keeps Koin out of `composeApp/commonMain` constructors.

### Domain ↔ DTO boundary

- DTOs in `shared/commonMain/com/frame/zero/dto/<feature>/`; domain in `.../domain/<feature>/`.
  Map via sibling `*Mapper.kt`.
- UI hints (accent colors, badge labels) are extension functions on the domain enum, never DTO
  fields. Wire format carries only semantic fields — derive cosmetics client-side in mappers.

### Wire contract duplication

Wire DTOs/enums are **deliberately duplicated, not shared**: client copy in `shared/commonMain`
(`com.frame.zero.dto.*`, `auth.dto.*`, enums `Genre`/`ProductionPhase`/`ProductionSort`/
`ScheduleEventKind`/`DevicePlatform`), server copy in `server/src/main` under the **same package
names**. Same FQN from separate source = no build dependency; server is a leaf so no classpath
collision. **Price: keep in sync by hand — edit both copies in the same change** or they drift.
(`Constants`/`SERVER_PORT` is client-only; server hardcodes its bind port.)

### Offline-first repositories

Paginated lists = **Room (KMP) + Paging 3 `RemoteMediator`**; `shared/repositories/productions`
is the reference impl.

- Repo returns `Flow<PagingData<DomainType>>`. `*Impl` builds a `Pager` whose
  `pagingSourceFactory` is a Room DAO query and `remoteMediator` writes API pages into Room
  (REFRESH replaces, APPEND cursor-paginates). UI observes Room, never the network.
- Room layer in the same module under `local/` (`FrameZeroDatabase`, `*Entity`, `*Dao`,
  `*RemoteKeyEntity`, `*EntityMapper`). Apply `libs.plugins.ksp` + `libs.plugins.androidxRoom`,
  `@Database(exportSchema = false)`, point Room plugin at `build/schemas` (required even when
  export off), register `kspAndroid`/`kspIosArm64`/`kspIosSimulatorArm64` (no `kspJvm`). No
  schema JSONs/migrations until production — flip `exportSchema = true` then.
- Room's KMP builder is platform-specific: `interface DatabaseBuilderFactory` in `commonMain`
  with `Android*`/`Ios*` actuals via `platformModule()`. Koin sets `BundledSQLiteDriver` +
  `Dispatchers.Default` query context.
- **Sign-out cleanup:** any module owning user-scoped local state implements
  `interface SessionCleaner { suspend fun clear() }` (`core/session/`) and registers via
  `single { … } bind SessionCleaner::class`. `SessionManager` runs each under its own
  `runCatching` in `forceLogout()`; tokens cleared *after*. Ref: `ProductionsSessionCleaner`.

### Self-registering plugins

Cross-cutting concerns are plugins so a backend toggles in one DI line. Three parts: a **sink
contract** interface, a **facade** `single` the app calls (which receives `getAll<Sink>()` and
fans out to each sink under its own `runCatching`), and **registration**
`single { SomeSink(...) } bind XSink::class`. Add a backend = implement the sink + one `bind`
line. Instances: `SessionCleaner` (`core/session/`), `LogSink`→`Logger` (`core/logging/`),
`AnalyticsSink`→`Analytics` (`core/analytics/`). Reference impls in
`shared/integrations/firebase` (opt-in; Android build needs gitignored Firebase config files).
Empty `getAll()` = no-op.

### Biometric app lock

Gates an *already* logged-in session (independent of `SessionManager`); hides sensitive data
behind Face ID / fingerprint on cold start and return-to-foreground. Lives in `core/security/`.

- `BiometricAuthenticator` is a platform interface (Android `BiometricPrompt`, iOS `LAContext`)
  via `platformModule()`. Android's impl needs a `FragmentActivity` from `ActivityHolder` (in
  `FrameZeroApp`), so **`MainActivity` extends `FragmentActivity`, not `ComponentActivity`**.
- `AppLockManager` (commonMain, pure) owns the policy (persisted flag in encrypted `Settings` +
  `AppLockState` flow). `RootComponent` combines it with the session into `isLocked`, re-locks
  on `lifecycle.doOnStop` (Android) / `UIApplicationDidEnterBackground` (iOS), renders
  `BiometricLockOverlay` (sign-out button is the escape hatch).
- Toggle in Account screen; both directions need a successful prompt. Prompt copy resolved from
  string resources at the UI layer and passed down — `shared` never touches Compose resources.
- **iOS needs `NSFaceIDUsageDescription` in `iosApp` Info.plist** (Swift step, manual edit).

### Module placement

Cross-cutting domain/network/storage → `shared/`. Repos → `shared/repositories/<name>/`.
Feature logic → `shared/features/<name>/`; feature UI → `composeApp/features/<name>/`. Root
nav/theming → `composeApp/commonMain`. Server → `server/`. DTOs/wire enums → both hand-synced
copies. New feature = both halves, registered in `settings.gradle.kts`.

### Convention plugins (build-logic)

- `crossplatform.library` — `com.android.library` + `kotlinMultiplatform`, registers
  `androidTarget`/`iosArm64`/`iosSimulatorArm64`, catalog SDK/JVM versions, code quality.
- `crossplatform.library.compose` — above + Compose Multiplatform + standard Compose deps.

New KMP library modules apply one of these, not hand-configured targets.

### Expect / Actual

For platform behaviour, declare `expect` in `commonMain` and provide `androidMain` **and**
`iosMain` actuals in the same change (no `jvmMain` — no module targets the JVM). No TODO
actuals. Don't `if/when` on runtime OS.

### Working principle

Owner is an Android engineer with limited iOS/backend experience. **Maximise Kotlin sharing —
`commonMain` first; platform source sets only when a Kotlin API genuinely doesn't exist.** Don't
silently drop code into `androidMain` "for now". When in doubt, ask.

### Tests

`commonTest/` uses `kotlin.test`. ViewModel/repository tests use a per-feature `testing/Fakes.kt`
(see `shared/features/auth/.../testing/Fakes.kt`). No mockito/mockk in shared code.

### Screenshot tests

Roborazzi goldens, piloted in `composeApp/shared/design_system`: `PreviewScreenshotTest`
auto-captures every `@LightDarkPreview`/`@Preview` (Light + Dark). Goldens committed under
`<module>/src/androidUnitTest/screenshots/`. Exact-match suite — exclude flaky animated previews
by `methodName` via the test's `ANIMATED_PREVIEWS` set.

## Key dependencies

All versions in `gradle/libs.versions.toml` — add new deps to the catalog, not module scripts.

- Kotlin **2.4.0**, Compose MP **1.11.1**, AGP **9.2.1**, KSP **2.3.8**
- Ktor **3.5.0** (Netty server; OkHttp Android, Darwin iOS client)
- Material3 `1.10.0-alpha05`, Decompose **3.5.0**, Koin **4.2.1**
- `multiplatform-settings` **1.3.0**, `kotlinx-datetime` **0.8.0** (`LocalDate` in DTOs;
  timestamps `kotlin.time.Instant` — never `java.time.*`)
- Room **2.8.4** (KMP) + Paging **3.5.0** + SQLite bundled **2.6.2**
- Server: Exposed **1.3.0** + HikariCP **7.0.2** + PostgreSQL **42.7.11** + Flyway; JWT via
  `ktor-server-auth-jwt`; bcrypt; tests use Testcontainers Postgres
- Tests: Robolectric **4.16.1**, Roborazzi **1.63.0** + ComposablePreviewScanner **0.9.0**
- Android minSdk **29**, compile/targetSdk **37**, JVM target **11**

### Dependency verification

`gradle/verification-metadata.xml` pins a sha256 for every artifact; its presence makes Gradle
verify every download — tampering fails the build. Don't hand-edit. **Regenerate whenever you
change any dependency/plugin version** or the build fails:

```bash
FRAMEZERO_API_BASE_URL=https://api.framezero.invalid \
  ./gradlew --write-verification-metadata sha256 --no-configuration-cache \
    check :composeApp:shared:design_system:verifyRoborazziDebug \
    :composeApp:assembleRelease
```

(Needs Docker up + macOS for iOS klibs.) Then review the `git diff` — added checksums should
match only the bumped deps — and commit it with the version change.

## Server config

`AppConfig.fromEnv()` reads env vars. Dev (`io.ktor.development=true`, set by `:server:run`)
supplies a hardcoded `JWT_SECRET`; production (`KTOR_ENV=production`) fails fast without one.
DB/JWT vars default to `docker-compose.yml`. Access-token TTL 15 min, refresh 30 days
(`JwtConfig`).

`FIREBASE_CREDENTIALS_PATH` (Firebase service-account JSON) is **required at boot in every
mode** (`AppConfig.validate()` fails fast) — task assignments send FCM pushes via
`FirebaseAdminPushSender`. Get it from Firebase console → Service accounts. Tests inject a fake
`PushSender` and don't need it.

```bash
# Local dev — DB defaults work; Firebase creds required
export FIREBASE_CREDENTIALS_PATH=/absolute/path/to/service-account.json
docker compose up -d && ./gradlew :server:run

# Production-style
export KTOR_ENV=production JWT_SECRET=$(openssl rand -base64 32)
export DATABASE_URL=jdbc:postgresql://prod-host:5432/framezero
export DATABASE_USER=... DATABASE_PASSWORD=...
export FIREBASE_CREDENTIALS_PATH=/run/secrets/firebase-service-account.json
./gradlew :server:run
```

### Schema

**Flyway owns DDL** — `DatabaseFactory.init` migrates versioned SQL in
`server/src/main/resources/db/migration` on boot. Exposed `Table` defs are the read/write
mapping only. Adding schema: add a `V<n>__*.sql` migration **and** update the matching Exposed
`Table` in the same change. Repo tests run against Testcontainers Postgres (`:server:test`, needs
Docker).

## Client storage

- `multiplatform-settings` for small k/v (auth tokens, prefs).
- **Room (KMP)** for paginated/offline-first; each db in its owning repo module. Don't add a
  second persistence library (SQLDelight, Realm, …) without confirming.

## Design system

All `composeApp` UI goes through `AppTheme` (wrap the root; handles dark/light via
`isSystemInDarkTheme()`). Full token list in `composeApp/shared/design_system/`
(`ColorSystem.kt` / `ThemeOptions.kt`).

```kotlin
AppTheme.colorSystem.<token>      // background, textPrimary, accent, errorText, …
AppTheme.typographySystem.<token> // titleSection, bodyStandard, bodySmall, label, button
AppTheme.spacingSystem.<token>    // xxs xs sm md lg xl xxl x3l x4l x5l x6l
AppTheme.radiusSystem.<token>     // xs sm segItem md lg input button card sheet circle
```

- Never use `MaterialTheme.colorScheme`/`typography`, hardcoded `Color(0xFF…)`, or raw `dp`/`sp`
  for visual tokens. Pick the closest semantic token; add one to `ColorSystem` + `ThemeOptions`
  if none fits.
- When a sub-system (`colorSystem`/`spacingSystem`/`typographySystem`) is used more than once in
  a composable, extract a local `val` at the top. Single-use can be inline.
- `spacingSystem` only for spacing (padding, gaps, spacers) — never element/border sizes; hoist
  those to top-of-file `Dp` `val`s.
- Gaps: `VerticalSpacer`/`HorizontalSpacer(AppTheme.spacingSystem.spaceN)` from
  `...design_system.widgets`, never raw `Spacer`.
- Clickable surfaces: `Modifier.clickableWithRipple(color = ...)` from `...design_system.modifier`,
  not bare `Modifier.clickable`.
- Previews: `@LightDarkPreview` (from `...design_system`), not plain `@Preview`.
- Extracted composables: screen-local in `components/` next to the parent; feature-shared in
  `ui/components/`. One primary `internal` composable + a `@LightDarkPreview` per file.
- New composables get a default `Modifier` param; hoist magic numbers to top-of-file `val`s.
- Always `collectAsStateWithLifecycle()`, not `collectAsState()`.

## Conventions

- **Readability:** descriptive names, small single-purpose functions, straightforward control flow.
- **Async:** `suspend fun` for one-shots, `Flow` for streams. Don't expose `Deferred`/callbacks
  across module boundaries.
- **Errors:** canonical cross-boundary result is `Outcome<T>` (`shared/.../domain/Outcome.kt`);
  reserve `Result<T>` for ad-hoc internal flows. Never throw across modules. Server maps
  exceptions to HTTP via `AppException`.
- **StateFlow:** always `_state.update { it.copy(...) }`, never `_state.value = ...` (atomic).
- **Serialization:** kotlinx.serialization; `@Serializable` on DTOs.
- **Presentation logic lives in the ViewModel:** date/number formatting, label/initials/display
  strings (as `private` members) — never in `*Ui.kt`/`*Mapper.kt`. Mappers (`toUi`, `toState`)
  do only structural field/enum mapping. Ref: `ScheduleTabViewModel`.
- **DI:** each feature exposes `val <name>Module = module { ... }`, registered in
  `composeApp/.../di/AppModule.kt`. ViewModels `factory`, repositories `single`. No Koin in
  `composeApp/commonMain` constructors — inject component factories.
- **Interface + Impl naming:** `<Interface>Impl` side-by-side in the same file/package; no
  tech/strategy suffixes (`*Exposed`, `Ktor*`, `OfflineFirst*`). Exception: platform actuals in
  `androidMain`/`iosMain` keep the platform prefix (`AndroidDatabaseBuilderFactory`).
- **HTTP engine** via `expect/actual` in `core/network/HttpClientFactory.kt` (OkHttp/Darwin).
- **Resources:** Compose resources in `composeApp/src/commonMain/composeResources/`, via `Res`.
- **Strings from shared code:** ViewModels emit `UiText` (`UiText.Dynamic(text)` or
  `someRes.asUiText(args…)`); UI resolves via `UiText.asString()`. Never resolve resources to raw
  `String` in `shared`.
- **Lifecycle/ViewModel:** shared ViewModels implement Essenty `InstanceKeeper.Instance` (manual
  `CoroutineScope` cancelled in `onDestroy()`); the Component creates one via
  `instanceKeeper.getOrCreate { factory() }`. Not `androidx.lifecycle viewModel { }`.
- **No `LocalContext.current`** in `composeApp/commonMain` — use an `expect`/`actual` in `shared`.

## Avoid

- `java.time.*` in shared code (use `kotlinx-datetime`).
- Exposing Compose UI types from `shared/`.
- SwiftUI changes unless asked.
- Domain logic on a DTO — map DTO→domain in a `*Mapper.kt`; keep the DTO a dumb wire shape.
