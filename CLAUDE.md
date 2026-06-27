# CLAUDE.md

Kotlin Multiplatform app (Android + iOS) with a Ktor/Postgres server. Compose Multiplatform UI, Decompose navigation, Koin DI. Composite Gradle build (`build-logic/`).

Owner is an Android engineer with limited iOS/backend experience. **Maximise Kotlin sharing: `commonMain` first; platform source sets only when no multiplatform API exists.** When in doubt, ask.

- **Server needs `FIREBASE_CREDENTIALS_PATH`** (service-account JSON) to boot in every mode; `FILE_STORAGE_DIR` (default `./uploads`) holds attachment blobs. Prod (`KTOR_ENV=production`) also needs `JWT_SECRET` + DB vars.
- **iOS:** open `iosApp/iosApp.xcodeproj` in Xcode. Relink after `shared`/`composeApp` changes: `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`. Don't touch SwiftUI unless asked.
- **Screenshot tests (Roborazzi):** `check` does NOT verify — run `:composeApp:shared:design_system:verifyRoborazziAndroidHostTest` (or `recordRoborazziAndroidHostTest` to regen goldens) explicitly. These are KMP modules, so Roborazzi tasks carry the `AndroidHostTest`/`IosSimulatorArm64` test-compilation suffix, not the AGP-style `Debug` variant suffix.
- **Dependency verification:** `gradle/verification-metadata.xml` pins sha256 for every artifact. Regenerate on any dep/plugin bump (command + flags in git history) or the build fails.

## Modules

| Module | Purpose |
|--------|---------|
| `shared/` | Multiplatform logic, no UI: domain models, DTO↔domain mappers, `UseCase` base, Ktor client (`core/network/`), session machine (`core/session/`), cross-cutting `core/*`. |
| `shared/features/<name>/` | Per-feature logic: Decompose `Component`, ViewModel, state/intent, Koin module. |
| `shared/repositories/<name>/` | Repository interface + `*Impl`. `productions` is the offline-first reference. |
| `shared/database/` | Single shared Room DB (`FrameZeroDatabase`, all entities/DAOs, `databaseModule`). Depends on no app module. |
| `shared/test-fixtures/` | Cross-feature fakes/builders (`com.frame.zero.testing`). |
| `shared/ui_text/` | `UiText` so shared ViewModels carry strings without Compose. |
| `shared/integrations/<name>/` | Self-registering plugins (e.g. `firebase` = Crashlytics/Analytics sinks). |
| `composeApp/` | Compose MP host: `App.kt`, `RootComponent`, platform entry points. |
| `composeApp/features/<name>/` | Stateless Compose UI for the matching shared component. |
| `composeApp/shared/design_system/` | Design tokens + widgets. |
| `server/` | JVM Ktor (Netty + Exposed/Postgres + JWT). Depends on no client module. |

**Features:** `account`, `auth`, `home`, `production`, `production-details`, `task-create`, `task-details` (each has a `shared/features/` + `composeApp/features/` half).
**New feature** = both halves + Koin module, registered in `settings.gradle.kts`, wired into `RootComponent`.

## Architecture

- **Navigation = Decompose.** `RootComponent` (`composeApp/commonMain`) owns a `StackNavigation` and swaps the whole stack on `SessionState`. Constructor order: `ComponentContext` first, then nav callbacks, then ViewModel factories. Sub-nav via sibling sealed `Config`/`Child`. No Koin in `composeApp/commonMain` constructors — inject component factories.
- **Layering:** ViewModel → `UseCase<Params,T>`/`NoParamsUseCase<T>` (`execute()` wrapped to `Outcome<T>`) → repository. Don't call repos from ViewModels; don't re-wrap use cases in try/catch.
- **Wire DTOs are duplicated, not shared:** client copy in `shared/commonMain` (`com.frame.zero.dto.*`, `auth.dto.*`, enums `Genre`/`ProductionPhase`/`ProductionSort`/`ScheduleEventKind`/`DevicePlatform`) and server copy under the same package names. **Edit both copies in the same change** or they drift.
- **UI hints (colors, badges) are domain-enum extension functions, derived in mappers** — never wire fields. Mappers do structural mapping only; presentation logic (formatting, labels) lives in the ViewModel.
- **Offline-first lists:** Room + Paging 3 `RemoteMediator`; repo returns `Flow<PagingData<Domain>>`; UI observes Room. Reference: `shared/repositories/productions`.
- **Offline-first writes:** task creation is queued via `core/upload/` (Room `pending_uploads` + platform `TaskUploadScheduler`), survives process death. Reference: `task-create`.
- **Self-registering plugins:** sink interface + facade `single` (`getAll<Sink>()`, fans out under `runCatching`) + `single { … } bind XSink::class`. Instances: `SessionCleaner`, `LogSink`→`Logger`, `AnalyticsSink`→`Analytics`.
- **Sign-out cleanup:** any module with user-scoped local state implements `SessionCleaner` and binds it; `SessionManager.forceLogout()` runs each before clearing tokens.
- **Server schema:** Flyway owns DDL (`server/.../db/migration/V<n>__*.sql`); Exposed `Table` is read/write mapping only — edit both in the same change.

## Conventions

- **DI:** each feature exposes `val <name>Module = module { }`, registered in `composeApp/.../di/AppModule.kt`. ViewModels `factory`, repositories `single`.
- **expect/actual:** declare in `commonMain`, provide `androidMain` + `iosMain` actuals in the same change. No `jvmMain` (no module targets JVM). No TODO actuals; don't branch on runtime OS.
- **Errors:** `Outcome<T>` across boundaries; never throw across modules. Server maps to HTTP via `AppException`.
- **StateFlow:** always `_state.update { it.copy(...) }`, never `_state.value = …`.
- **Lifecycle:** shared ViewModels implement Essenty `InstanceKeeper.Instance` (manual scope); UI uses `collectAsStateWithLifecycle()`.
- **Strings from shared:** ViewModels emit `UiText`; UI resolves via `UiText.asString()`. Never resolve resources to `String` in `shared`.
- **Dependencies:** add to `gradle/libs.versions.toml`, not module scripts.
- **Naming:** `<Interface>Impl` side-by-side; no tech/strategy suffixes (platform actuals keep their prefix).
- **Tests:** `kotlin.test` in `commonTest`; reuse `shared/test-fixtures` fakes. No mockk/mockito in shared code.
- **Idiomatic Kotlin:** prefer idiomatic constructs (`?.let`/`?:`, `takeIf`, `require`/`check`/`error`, collection ops, destructuring, string templates, expression bodies) — but only when they improve readability, never to golf.

### Design system

All `composeApp` UI goes through `AppTheme`. Use `AppTheme.colorSystem/typographySystem/spacingSystem/radiusSystem.<token>`.

- Never `MaterialTheme.*`, hardcoded `Color(0xFF…)`, or raw `dp`/`sp` for visual tokens. Add a token if none fits.
- `spacingSystem` for spacing only — hoist element/border sizes to top-of-file `Dp` vals. Extract a local `val` when a sub-system is used more than once.
- Gaps: `VerticalSpacer`/`HorizontalSpacer(…)`, not raw `Spacer`. Clickables: `Modifier.clickableWithRipple(…)`, not bare `clickable`.
- Previews: `@LightDarkPreview`. Extracted composables are `internal`, one per file with its own preview, in `components/` (screen-local) or `ui/components/` (feature-shared).

## Avoid

- `java.time.*` in shared code (use `kotlinx-datetime`); `kapt` (use `ksp`); `LocalContext.current` in `composeApp/commonMain`.
- Java/Android-only libs in `commonMain`; SQLDelight/Realm (Room is the DB, `multiplatform-settings` for small k/v).
- Sharing the wire DTOs via a common module; domain logic on a DTO.
- SwiftUI changes unless asked; hand-authoring SVG/vector icons (ask for the asset).
