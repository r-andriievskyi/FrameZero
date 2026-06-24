# AGENTS.md

## Project Overview

FrameZero is a Kotlin Multiplatform (KMP) app targeting Android and iOS, with a Ktor server backend. Navigation uses Decompose; DI uses Koin; UI uses Compose Multiplatform with a custom design system.

## Architecture — Key Decisions

- **Maximise Kotlin sharing.** Default to `commonMain`; use platform source sets only when no multiplatform API exists.
- **Decompose for navigation:** `RootComponent` in `composeApp/commonMain` owns a `StackNavigation`. Feature components live in `shared/features/<name>/`. Feature UI in `composeApp/features/<name>/` is stateless — all logic stays in `shared`.
- **Wire types are deliberately duplicated, not shared:** the wire DTOs and enums (`Genre`, `ProductionPhase`, `ProductionSort`, `ScheduleEventKind`, `DevicePlatform`) exist as **two hand-synced copies under identical package names** — client copy in `shared/commonMain` (`com/frame/zero/dto/`, `auth/dto/`), server copy in `server/src/main` (`com/frame/zero/dto/`). The server has **no client-module dependency** so it can be lifted into a standalone repo. When you change a wire shape (add/rename/remove a field, change an enum), **edit both copies in the same change** or client and server silently drift. DTOs are organised by domain subdirectory (`common/`, `dashboard/`, `production/`, `schedule/`, `notification/`, `task/`, `device/`). `Constants`/`SERVER_PORT` is client-only (not duplicated).
- **Self-registering plugins:** cross-cutting concerns (logging, analytics, sign-out cleanup) use a sink + facade + `bind` pattern. Implement the sink interface (`LogSink`, `AnalyticsSink`, `SessionCleaner`) and register it with `single { … } bind <Sink>::class`; the facade (`Logger`, `Analytics`, `SessionManager`) injects `getAll<Sink>()` and fans out to each under its own `runCatching`. Adding a backend (e.g. Crashlytics) is one `bind` line. Reference sinks: `shared/integrations/firebase/` (`FirebaseCrashlyticsLogSink`, `FirebaseAnalyticsSink`).
- **Strings from shared code:** shared ViewModels can't call `stringResource`, so they emit `UiText` (`shared/ui_text/`) — `UiText.Dynamic(text)` for resolved strings, `someRes.asUiText(args…)` for a resource ref. The UI resolves it with `UiText.asString()` (`composeApp/shared/ui_text/`) at render time. Never resolve resources into raw `String`s inside `shared`.
- **Convention plugins** (`build-logic/`): `crossplatform.library`, `crossplatform.library.compose`, and `crossplatform.code.quality` configure targets, SDK versions, and code quality tooling. New KMP modules should apply one of the first two (they inherit code quality automatically).
- **Server DB migrations:** Flyway manages schema evolution. Migration files live in `server/src/main/resources/db/migration/` using the naming convention `V<N>__<description>.sql`.
- **Offline-first repositories:** Paginated lists use Room (KMP) + Paging 3 `RemoteMediator`. `shared/repositories/productions/` is the reference impl. The repo returns `Flow<PagingData<DomainType>>`; UI observes Room, never the network directly. The single Room database (`FrameZeroDatabase`) and all entities/DAOs now live in **one shared leaf module, `shared/database/`** — not per-repo `local/` packages — so multiple features share it. That module applies `libs.plugins.ksp` + `libs.plugins.androidxRoom`, registers `ksp` configs for all targets, exposes `databaseModule` + a platform `DatabaseBuilderFactory`, and depends on **no app module** (so add a new entity/DAO there, then `api(projects.shared.database)` from the consuming repo).
- **Use-case layer:** ViewModels don't call repositories directly — they invoke single-purpose use cases extending `UseCase<Params, T>` / `NoParamsUseCase<T>` (`shared/.../domain/UseCase.kt`), whose `invoke()` wraps `execute()` in `runCatching` and returns `Outcome<T>` (errors mapped to `DomainError`). Use cases live in the feature's `domain/` or `usecase/` package (e.g. `CreateTaskUseCase`, `GetAssignableMembersUseCase`) and are registered `factory` in the feature's Koin module.
- **Offline-first writes (background uploads):** Task creation (with optional attachment) is queued, not awaited. `core/upload/` records the upload in `PendingUploadStore` (Room `pending_uploads` table in `shared/database/`) and hands it to a platform `TaskUploadScheduler` (`expect`/`actual`: Android WorkManager, iOS background `NSURLSession`) so it survives navigation and process death. `task-create` is the reference feature.
- **Push token sync:** `core/push/PushTokenProvider` (platform FCM/APNs token) feeds `shared/repositories/device-token/`. `DeviceTokenSynchronizer` (eager `single`) registers the token with the server on every `LoggedIn` transition and on rotation; `DeviceTokenSessionCleaner` clears it on logout. The current `DevicePlatform` is resolved via an `expect`/`actual` `devicePlatform()`.
- **Sign-out cleanup:** Any module owning user-scoped local state (Room cache, in-memory user data) implements `SessionCleaner` (`shared/.../core/session/SessionCleaner.kt`) and registers in its Koin module with `single { … } bind SessionCleaner::class`. `SessionManager` invokes every registered cleaner — each under its own `runCatching` — before clearing tokens in `forceLogout()`, covering both user-initiated sign-out and the auto-logout path triggered by a failed refresh-token rotation. Reference impls: `ProductionsSessionCleaner` → `ProductionsDao.clearAll()`, `DeviceTokenSessionCleaner`.

## Adding a New Feature

1. Create `shared/features/<name>/` with a `build.gradle.kts` applying `crossplatform.library`.
2. Create `composeApp/features/<name>/` with a `build.gradle.kts` applying `crossplatform.library.compose`.
3. Register both in `settings.gradle.kts` (e.g. `include(":shared:features:<name>")`, `include(":composeApp:features:<name>")`).
4. Add a Koin module (see `shared/features/auth/src/commonMain/.../AuthModule.kt` as reference).
5. Wire the new component into `RootComponent`'s `ChildStack`.

## Commands

```bash
./gradlew :composeApp:installDebug     # Install Android debug build on a connected device
./gradlew :server:run                  # Ktor server on port 8080
./gradlew ktlintFormat                 # Auto-format all Kotlin source before committing
./gradlew ktlintCheck                  # Verify formatting (CI)
./gradlew detekt                       # Static analysis
./gradlew check                        # Full CI gate: ktlintCheck + detekt + tests
```

## Patterns to Follow

- **Koin DI:** Each feature exposes a `val <name>Module = module { ... }` (see `authModule` in `AuthModule.kt`). ViewModels are `factory`, repositories are `single`.
- **Decompose components:** Accept `ComponentContext` + ViewModel factories via constructor. Use sealed `Config`/`Child` interfaces for sub-navigation (see `AuthComponent.kt`).
- **Expect/Actual:** When adding one, implement both actuals (`androidMain`, `iosMain`) in the same change. No module targets the JVM anymore (`shared` dropped its `jvm()` target once `:server` was decoupled), so an `expect` needs exactly those two actuals — no `jvmMain`. No TODO actuals; don't branch on runtime OS.
- **Design system locals:** When a design system sub-system (`colorSystem`, `spacingSystem`, `typographySystem`) is referenced more than once inside a composable, extract it to a local val at the top of the composable body (e.g. `val colorSystem = AppTheme.colorSystem`). Single-use references can be accessed inline.
- **Design system:** Use `AppTheme.colorSystem.*`, `AppTheme.spacingSystem.*`, `AppTheme.radiusSystem.*`, `AppTheme.typographySystem.*`. Never use `MaterialTheme` or hardcoded `Color`/`dp` values in feature UI. Use `spacingSystem` tokens only for spacing (padding, gaps) — never for size, width, or border-width values.
- **Spacers:** Use `VerticalSpacer(AppTheme.spacingSystem.spaceN)` and `HorizontalSpacer(AppTheme.spacingSystem.spaceN)` from the design system (`com.frame.zero.shared.design_system.widgets`). Never use a raw `Spacer(Modifier.height/width(...))`.
- **Clickable:** Use `Modifier.clickableWithRipple(color = ...)` from `com.frame.zero.shared.design_system.modifier` instead of the bare `Modifier.clickable { ... }` so every clickable surface gets a themed Material 3 ripple (`bounded`/`radius` configurable).
- **Previews:** Always use `@LightDarkPreview` (from `com.frame.zero.shared.design_system`) instead of a plain `@Preview`. This generates both light and dark previews automatically.
- **Components directory:** Composables extracted from a screen live in a `components/` subdirectory next to their parent screen file (e.g. `signin/components/SignInHeader.kt` alongside `signin/SignInContent.kt`). Composables shared across multiple screens within the same feature live in the feature-level `ui/components/` package (e.g. `ui/components/AuthLogoHeader.kt`). Every component file defines exactly one primary composable, is marked `internal`, and carries its own `@LightDarkPreview`.
- **Use cases:** ViewModel → use case → repository. New business actions are a `UseCase<Params, T>`/`NoParamsUseCase<T>` subclass implementing `execute()` (see `CreateProductionUseCase`); `invoke()` already returns `Outcome<T>`, so don't re-wrap in try/catch.
- **Errors:** Use `Outcome<T>` (sealed type in `shared/.../domain/Outcome.kt`) or `Result<T>` across layer boundaries; don't throw. Use cases map failures to `DomainError` automatically.
- **Test fakes:** Cross-feature fakes/builders live in the shared `:shared:test-fixtures` module (`com.frame.zero.testing` — `FakeAuthRepository`, `FakeTasksRepository`, `Builders.kt`). Reuse these in `commonTest` instead of re-writing repository fakes per feature.
- **StateFlow updates:** Always use `_state.update { it.copy(...) }` instead of `_state.value = _state.value.copy(...)`. The `update` function is atomic and avoids race conditions with concurrent emissions.
- **Flow collection in composables:** Always use `collectAsStateWithLifecycle()` instead of `collectAsState()`. It stops collection when the UI is not visible, saving CPU/battery.
- **Serialization:** `kotlinx.serialization` with `@Serializable` on shared DTOs.
- **Dependencies:** Always add to `gradle/libs.versions.toml`, never inline in build scripts.

## Things to Avoid

- No `kapt` — use `ksp`.
- No Java-only or Android-only libraries in `commonMain`.
- No `LocalContext.current` in shared Compose code.
- No `java.time.*` in shared code — use `kotlinx-datetime`.
- Don't try to share the wire DTOs via a common module — they are **intentionally duplicated** per side (see Architecture). Just keep the two copies in sync by hand.
- No SwiftUI code unless explicitly requested.
- No SQLDelight or Realm — Room KMP is the local DB; `multiplatform-settings` for small k/v storage.

## Key File Locations

| What | Where |
|------|-------|
| Version catalog | `gradle/libs.versions.toml` |
| Convention plugins | `build-logic/src/main/kotlin/` |
| Domain models | `shared/src/commonMain/kotlin/com/frame/zero/domain/` |
| Use-case base classes | `shared/src/commonMain/kotlin/com/frame/zero/domain/UseCase.kt` |
| Shared Room database | `shared/database/` (`FrameZeroDatabase`, entities, DAOs, `databaseModule`) |
| Shared test fakes/builders | `shared/test-fixtures/` (`com.frame.zero.testing`) |
| Shared DTOs | `shared/src/commonMain/kotlin/com/frame/zero/dto/` |
| Root navigation | `composeApp/src/commonMain/` (look for `RootComponent`) |
| Design system tokens | `composeApp/shared/design_system/` |
| Detekt config | `config/detekt/detekt.yml` |
| Flyway migrations | `server/src/main/resources/db/migration/` |

## Current Modules

**Features** (shared logic + compose UI):
`account`, `auth`, `home`, `production`, `production-details`, `task-create`, `task-details`

**Repositories** (`shared/repositories/<name>/`):
`auth`, `user`, `dashboard`, `productions`, `schedule`, `tasks`, `device-token`

**Server domains** (`server/src/main/kotlin/com/frame/zero/`):
`auth`, `dashboard`, `notification`, `production`, `schedule`, `task` (plus `storage` for file/attachment blobs)

**Cross-cutting / shared libs:**
`shared/database/` (single shared Room DB), `shared/test-fixtures/` (cross-feature fakes), `shared/ui_text/` + `composeApp/shared/ui_text/` (UiText), `shared/integrations/firebase/` (logging/analytics sinks), `composeApp/shared/design_system/`

