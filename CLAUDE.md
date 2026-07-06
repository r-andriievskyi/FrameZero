# CLAUDE.md

Kotlin Multiplatform app (Android + iOS), Ktor/Postgres server. Compose Multiplatform UI, Decompose nav, Koin DI. Composite Gradle build (`build-logic/`).

Owner is Android engineer, limited iOS/backend experience. **Maximise Kotlin sharing: `commonMain` first; platform source sets only when no multiplatform API exists.** When in doubt, ask.

- **Server needs `FIREBASE_CREDENTIALS_PATH`** (service-account JSON) to boot every mode; `FILE_STORAGE_DIR` (default `./uploads`) holds attachment blobs. Prod (`KTOR_ENV=production`) also needs `JWT_SECRET` + DB vars.
- **iOS:** open `iosApp/iosApp.xcodeproj` in Xcode. Relink after `shared`/`composeApp` changes: `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`. Don't touch SwiftUI unless asked.
- **Screenshot tests (Roborazzi):** `check` does NOT verify — run tasks explicitly. Live in `:composeApp:shared:design_system` **and every `composeApp/features/<name>` module**: each applies the `crossplatform.screenshot` convention plugin (Roborazzi + deps via `:composeApp:shared:screenshot-testing`) and keeps a small `androidHostTest/.../PreviewScreenshotTest.kt` subclass of `BasePreviewScreenshotTest` declaring its package tree + animated-preview exclusions, plus `androidHostTest/resources/robolectric.properties`. Run all with unqualified `./gradlew verifyRoborazziAndroidHostTest` (or `recordRoborazziAndroidHostTest` to regen goldens), or scope one module with `:composeApp:features:<name>:verifyRoborazziAndroidHostTest`. KMP modules, so Roborazzi tasks carry `AndroidHostTest`/`IosSimulatorArm64` test-compilation suffix, not AGP-style `Debug` variant suffix. Adding/removing `@Preview`/`@LightDarkPreview` changes golden set, so re-record after.

## Modules

| Module | Purpose |
|--------|---------|
| `shared/` | Multiplatform logic, no UI: domain models, DTO↔domain mappers, `UseCase` base, Ktor client (`core/network/`), session machine (`core/session/`), cross-cutting `core/*`. |
| `shared/features/<name>/` | Per-feature logic: Decompose `Component`, ViewModel, state/intent, Koin module. |
| `shared/repositories/<name>/` | Repository interface + `*Impl`. `productions` = offline-first reference. |
| `shared/database/` | Single shared Room DB (`FrameZeroDatabase`, all entities/DAOs, `databaseModule`). Depends on no app module. |
| `shared/test-fixtures/` | Cross-feature fakes/builders (`com.frame.zero.testing`). |
| `shared/ui_text/` | `UiText` so shared ViewModels carry strings without Compose. |
| `shared/integrations/<name>/` | Self-registering plugins (e.g. `firebase` = Crashlytics/Analytics sinks). |
| `composeApp/` | Compose MP host: `App.kt`, `RootComponent`, platform entry points. |
| `composeApp/features/<name>/` | Stateless Compose UI for matching shared component. |
| `composeApp/shared/design_system/` | Design tokens + widgets. |
| `server/` | JVM Ktor (Netty + Exposed/Postgres + JWT). Depends on no client module. |

**Features:** `account`, `auth`, `home`, `production`, `production-details`, `task-create`, `task-details` (each has `shared/features/` + `composeApp/features/` half).
**New feature** = both halves + Koin module, registered in `settings.gradle.kts`, wired into `RootComponent`.

## Architecture

- **Navigation = Decompose.** `RootComponent` (`composeApp/commonMain`) owns `StackNavigation`, swaps whole stack on `SessionState`. Constructor order: `ComponentContext` first, then nav callbacks, then ViewModel factories. Sub-nav via sibling sealed `Config`/`Child`. No Koin in `composeApp/commonMain` constructors — inject component factories.
- **Layering:** ViewModel → `UseCase<Params,T>`/`NoParamsUseCase<T>` (`execute()` wrapped to `Outcome<T>`) → repository. Don't call repos from ViewModels; don't re-wrap use cases in try/catch.
- **MVI input contract:** UI talks to ViewModel through **one** `fun onIntent(intent: <Feature>Intent)` channel — sealed `*Intent` per feature, never discrete `doX()` methods. `Component` = thin passthrough (`onIntent` + collect one-shot `events` → nav callbacks); must **not** read `viewModel.state.value` to decide — move logic into VM, surface result as event. One-shot effects use `events: SharedFlow<<Feature>Event>` (sealed, `MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = DROP_OLDEST)`). **Exception:** pure read surface with no user input may skip intents (reference: `home/productions`, only exposes `Flow<PagingData>`).
- **Wire DTOs duplicated, not shared:** client copy in `shared/commonMain` (`com.frame.zero.dto.*`, `auth.dto.*`, enums `Genre`/`ProductionPhase`/`ProductionSort`/`ScheduleEventKind`/`DevicePlatform`) and server copy under same package names. **Edit both copies in same change** or they drift.
- **UI hints (colors, badges) = domain-enum extension functions, derived in mappers** — never wire fields. Mappers do structural mapping only; presentation logic (formatting, labels) lives in ViewModel.
- **Offline-first lists:** Room + Paging 3 `RemoteMediator`; repo returns `Flow<PagingData<Domain>>`; UI observes Room. Reference: `shared/repositories/productions`.
- **Offline-first writes:** task creation queued via `core/upload/` (Room `pending_uploads` + platform `TaskUploadScheduler`), survives process death. Reference: `task-create`.
- **Self-registering plugins:** sink interface + facade `single` (`getAll<Sink>()`, fans out under `runCatching`) + `single { … } bind XSink::class`. Instances: `SessionCleaner`, `LogSink`→`Logger`, `AnalyticsSink`→`Analytics`.
- **Sign-out cleanup:** any module with user-scoped local state implements `SessionCleaner` and binds it; `SessionManager.forceLogout()` runs each before clearing tokens.
- **Server schema:** Flyway owns DDL (`server/.../db/migration/V<n>__*.sql`); Exposed `Table` = read/write mapping only — edit both in same change.

## Conventions

- **DI:** each feature exposes `val <name>Module = module { }`, registered in `composeApp/.../di/AppModule.kt`. ViewModels `factory`, repositories `single`.
- **expect/actual:** declare in `commonMain`, provide `androidMain` + `iosMain` actuals in same change. No `jvmMain` (no module targets JVM). No TODO actuals; don't branch on runtime OS.
- **Errors:** `Outcome<T>` across boundaries; never throw across modules. Server maps to HTTP via `AppException`.
- **StateFlow:** always `_state.update { it.copy(...) }`, never `_state.value = …`.
- **Lifecycle:** shared ViewModels implement Essenty `InstanceKeeper.Instance` (manual scope); UI uses `collectAsStateWithLifecycle()`.
- **Strings from shared:** ViewModels emit `UiText`; UI resolves via `UiText.asString()`. Never resolve resources to `String` in `shared`.
- **Dependencies:** add to `gradle/libs.versions.toml`, not module scripts.
- **Naming:** `<Interface>Impl` side-by-side; no tech/strategy suffixes (platform actuals keep prefix).
- **Tests:** `kotlin.test` in `commonTest`; reuse `shared/test-fixtures` fakes. No mockk/mockito in shared code.
- **Idiomatic Kotlin:** prefer idiomatic constructs (`?.let`/`?:`, `takeIf`, `require`/`check`/`error`, collection ops, destructuring, string templates, expression bodies) — only when they improve readability, never to golf.

### Design system

All `composeApp` UI goes through `AppTheme`. Use `AppTheme.colorSystem/typographySystem/spacingSystem/radiusSystem.<token>`.

- Never `MaterialTheme.*`, hardcoded `Color(0xFF…)`, or raw `dp`/`sp` for visual tokens. Add token if none fits.
- `spacingSystem` for spacing only — hoist element/border sizes to top-of-file `Dp` vals. Extract local `val` when sub-system used more than once.
- Gaps: `VerticalSpacer`/`HorizontalSpacer(…)`, not raw `Spacer`. Clickables: `Modifier.clickableWithRipple(…)`, not bare `clickable`.
- Previews: `@LightDarkPreview`. Extracted composables = `internal`, one per file with own preview, in `components/` (screen-local) or `ui/components/` (feature-shared).

## Avoid

- `java.time.*` in shared code (use `kotlinx-datetime`); `kapt` (use `ksp`); `LocalContext.current` in `composeApp/commonMain`.
- Java/Android-only libs in `commonMain`; SQLDelight/Realm (Room = DB, `multiplatform-settings` for small k/v).
- Sharing wire DTOs via common module; domain logic on DTO.
- SwiftUI changes unless asked; hand-authoring SVG/vector icons (ask for asset).