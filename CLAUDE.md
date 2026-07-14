# CLAUDE.md

Kotlin Multiplatform app (Android + iOS), Ktor/Postgres server. Compose Multiplatform UI, Decompose nav, Koin DI. Composite Gradle build (`build-logic/`).

Owner Android engineer, little iOS/backend experience. **Max Kotlin sharing: `commonMain` first; platform source sets only when no multiplatform API exist.** Doubt? Ask.

- **Gradle:** run all `./gradlew` tasks with `-q` (quiet). Cut noise.
- **Demo flavor:** Android `demo`/`prod` product flavors (same applicationId). `demo` run all on local fake data — no backend, no network — offline play. Build: `./gradlew :androidApp:assembleDemoDebug` or IDE variant picker. Flag reach shared code via BuildKonfig (`BuildFlags.IS_DEMO`), inferred from task name in `shared/build.gradle.kts` (any `*Demo*` task → `buildkonfig.flavor=demo`). Fakes + curated dataset live in `:shared:demo` (`demoModule`), wired in `AppModule` — `demoModule` loaded **last** so override real repo/scheduler bindings; auth = fake login screen (any credentials accepted). **One BuildKonfig flavor per Gradle invocation** — never build demo + prod variant tasks in same command (use `assembleProdRelease`, not `assembleRelease`). iOS: pass `-Pbuildkonfig.flavor=demo` to framework link task (no Xcode scheme yet).
- **Server need `FIREBASE_CREDENTIALS_PATH`** (service-account JSON) to boot every mode; `FILE_STORAGE_DIR` (default `./uploads`) hold attachment blobs. Prod (`KTOR_ENV=production`) also need `JWT_SECRET` + DB vars.
- **iOS:** open `iosApp/iosApp.xcodeproj` in Xcode. Relink after `shared`/`composeApp` change: `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`. No SwiftUI touch unless asked.
- **Screenshot tests (Roborazzi):** `check` NOT verify — run tasks explicit. Live in `:composeApp:shared:design_system` **and every `composeApp/features/<name>` module**: each apply `crossplatform.screenshot` convention plugin (Roborazzi + deps via `:composeApp:shared:screenshot-testing`), keep small `androidHostTest/.../PreviewScreenshotTest.kt` subclass of `BasePreviewScreenshotTest` declaring package tree + animated-preview exclusions, plus `androidHostTest/resources/robolectric.properties`. Run all: unqualified `./gradlew verifyRoborazziAndroidHostTest` (`recordRoborazziAndroidHostTest` regen goldens); scope one module: `:composeApp:features:<name>:verifyRoborazziAndroidHostTest`. KMP modules — Roborazzi tasks carry `AndroidHostTest`/`IosSimulatorArm64` test-compilation suffix, not AGP-style `Debug` variant suffix. Add/remove `@Preview`/`@LightDarkPreview` change golden set — re-record after.

## Modules

| Module | Purpose |
|--------|---------|
| `shared/` | Cross-cutting core, no UI: Ktor client (`core/network/`), session machine (`core/session/`), upload queue, security, logging/analytics — rest of `core/*`. Re-export `shared/domain` + `shared/dto` via `api`. |
| `shared/domain/` | Domain models, `Outcome`/`UseCase` base, `DomainError` + `toDomainError()`, `OfflineException`. Depend on no app module. |
| `shared/dto/` | Client wire DTOs (`com.frame.zero.dto.*`, `auth.dto.*`) + DTO→domain mappers. Depend only on `shared/domain`. |
| `shared/features/<name>/` | Per-feature logic: Decompose `Component`, ViewModel, state/intent, Koin module. |
| `shared/repositories/<name>/` | Repository contract; most modules interface-only (impls live in owning feature `data/`). `productions` + `chat` split `:api` (interface, features depend on this) / `:impl` (Ktor/Room/mappers/Koin module — only `composeApp` see it). **Contracts speak domain types only** (incl. command types `NewTask`/`NewProduction`) — DTO↔domain mapping inside impl, never in use cases/ViewModels. `productions` = offline-first reference. |
| `shared/database/` | Single shared Room DB (`FrameZeroDatabase`, all entities/DAOs, `databaseModule`). Depend on no app module. |
| `shared/test-fixtures/` | Cross-feature fakes/builders (`com.frame.zero.testing`). |
| `shared/demo/` | Demo-flavor fakes + curated offline dataset (`com.frame.zero.demo`): `DemoDataStore` in-memory source of truth, `Demo*Repository`/scheduler impls, `demoModule` Koin overrides. Compiled into all builds; wired only when `BuildFlags.IS_DEMO`. |
| `shared/ui_text/` | `UiText` — shared ViewModels carry strings without Compose. |
| `shared/integrations/<name>/` | Self-registering plugins (e.g. `firebase` = Crashlytics/Analytics sinks). |
| `androidApp/` | Android entry point (`com.android.application`): `MainActivity`, `FrameZeroApp`, FCM service, manifest/res, flavors + signing. |
| `composeApp/` | Compose MP host (`com.android.kotlin.multiplatform.library`): `App.kt`, `RootComponent`, iOS framework. |
| `composeApp/features/<name>/` | Stateless Compose UI for matching shared component. |
| `composeApp/shared/design_system/` | Design tokens + widgets. |
| `server/` | JVM Ktor (Netty + Exposed/Postgres + JWT). Depend on no client module. |

**Features:** `account`, `auth`, `home`, `production`, `production-details`, `task-create`, `task-details` (each has `shared/features/` + `composeApp/features/` half).
**New feature** = both halves + Koin module, registered in `settings.gradle.kts`, wired into `RootComponent`.

## Architecture

- **Navigation = Decompose.** `RootComponent` (`composeApp/commonMain`) own `StackNavigation`, swap whole stack on `SessionState`. Constructor order: `ComponentContext` first, then nav callbacks, then ViewModel factories. Sub-nav via sibling sealed `Config`/`Child`. No Koin in `composeApp/commonMain` constructors — inject component factories.
- **Layering:** ViewModel → `UseCase<Params,T>`/`NoParamsUseCase<T>` (`execute()` wrapped to `Outcome<T>`) → repository. No repo calls from ViewModels; no re-wrap use cases in try/catch.
- **MVI input contract:** UI talk to ViewModel through **one** `fun onIntent(intent: <Feature>Intent)` channel — sealed `*Intent` per feature, never discrete `doX()` methods. `Component` = thin passthrough (`onIntent` + collect one-shot `events` → nav callbacks); must **not** read `viewModel.state.value` to decide — move logic into VM, surface result as event. One-shot effects use `events: SharedFlow<<Feature>Event>` (sealed, `MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = DROP_OLDEST)`). **Exception:** pure read surface, no user input, may skip intents (reference: `home/productions`, only expose `Flow<PagingData>`).
- **Wire DTOs duplicated, not shared:** client copy in `shared/dto` (`com.frame.zero.dto.*`, `auth.dto.*`; wire enums `Genre`/`ProductionPhase`/`ProductionSort`/`ScheduleEventKind`/`TaskStatus`/`TaskPriority` live in `shared/domain`, `DevicePlatform` in its repo), server copy under original package names (`com.frame.zero.dto.task.TaskStatus` etc.). **Edit both copies in same change** or drift.
- **UI hints (colors, badges) = domain-enum extension functions, derived in mappers** — never wire fields. Mappers do structural mapping only; presentation logic (formatting, labels) live in ViewModel.
- **Offline-first lists:** Room + Paging 3 `RemoteMediator`; repo return `Flow<PagingData<Domain>>`; UI observe Room. Reference: `shared/repositories/productions`.
- **Offline-first writes:** task creation queued via `core/upload/` (Room `pending_uploads` + platform `TaskUploadScheduler`), survive process death. Reference: `task-create`.
- **Self-registering plugins:** sink interface + facade `single` (`getAll<Sink>()`, fan out under `runCatching`) + `single { … } bind XSink::class`. Instances: `SessionCleaner`, `LogSink`→`Logger`, `AnalyticsSink`→`Analytics`.
- **Sign-out cleanup:** any module with user-scoped local state implement `SessionCleaner`, bind it; `SessionManager.forceLogout()` run each before clearing tokens.
- **Server schema:** Flyway own DDL (`server/.../db/migration/V<n>__*.sql`); Exposed `Table` = read/write mapping only — edit both in same change.

## Conventions

- **DI:** each feature expose `val <name>Module = module { }`, registered in `composeApp/.../di/AppModule.kt`. ViewModels `factory`, repositories `single`.
- **expect/actual:** declare in `commonMain`, provide `androidMain` + `iosMain` actuals in same change. No `jvmMain` (no module target JVM). No TODO actuals; no runtime OS branching.
- **Errors:** `Outcome<T>` across boundaries; never throw across modules. Server map to HTTP via `AppException`.
- **StateFlow:** always `_state.update { it.copy(...) }`, never `_state.value = …`.
- **Lifecycle:** shared ViewModels implement Essenty `InstanceKeeper.Instance` (manual scope); UI use `collectAsStateWithLifecycle()`.
- **Strings from shared:** ViewModels emit `UiText`; UI resolve via `UiText.asString()`. Never resolve resources to `String` in `shared`.
- **Dependencies:** add to `gradle/libs.versions.toml`, not module scripts. Recurring groups via `[bundles]`: `koinRuntime` (koin-core+coroutines), `ktorClient` (core+negotiation+json), `ktorClientTest` (mock+negotiation+json), `commonTest` (kotlin-test+coroutines-test), `screenshotTest` — use bundle, no re-list members.
- **Naming:** `<Interface>Impl` side-by-side; no tech/strategy suffixes (platform actuals keep prefix).
- **Tests:** `kotlin.test` in `commonTest`; reuse `shared/test-fixtures` fakes. No mockk/mockito in shared code.
- **Idiomatic Kotlin:** prefer idiomatic constructs (`?.let`/`?:`, `takeIf`, `require`/`check`/`error`, collection ops, destructuring, string templates, expression bodies) — only when readability improve, never golf.

### Design system

All `composeApp` UI go through `AppTheme`. Use `AppTheme.colorSystem/typographySystem/spacingSystem/radiusSystem.<token>`.

- Never `MaterialTheme.*`, hardcoded `Color(0xFF…)`, or raw `dp`/`sp` for visual tokens. No token fit — add one.
- `spacingSystem` for spacing only — hoist element/border sizes to top-of-file `Dp` vals. Extract local `val` when sub-system used more than once.
- Gaps: `VerticalSpacer`/`HorizontalSpacer(…)`, not raw `Spacer`. Clickables: `Modifier.clickableWithRipple(…)`, not bare `clickable`.
- Previews: `@LightDarkPreview`. Extracted composables = `internal`, one per file with own preview, in `components/` (screen-local) or `ui/components/` (feature-shared).

## Avoid

- `java.time.*` in shared code (use `kotlinx-datetime`); `kapt` (use `ksp`); `LocalContext.current` in `composeApp/commonMain`.
- Java/Android-only libs in `commonMain`; SQLDelight/Realm (Room = DB, `multiplatform-settings` for small k/v).
- Sharing wire DTOs via common module; domain logic on DTO.
- SwiftUI changes unless asked; hand-authoring SVG/vector icons (ask for asset).