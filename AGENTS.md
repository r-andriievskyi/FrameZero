# AGENTS.md

## Project Overview

FrameZero is a Kotlin Multiplatform (KMP) app targeting Android and iOS, with a Ktor server backend. Navigation uses Decompose; DI uses Koin; UI uses Compose Multiplatform with a custom design system.

## Architecture — Key Decisions

- **Maximise Kotlin sharing.** Default to `commonMain`; use platform source sets only when no multiplatform API exists.
- **Decompose for navigation:** `RootComponent` in `composeApp/commonMain` owns a `StackNavigation`. Feature components live in `shared/features/<name>/`. Feature UI in `composeApp/features/<name>/` is stateless — all logic stays in `shared`.
- **Shared wire types:** DTOs and constants used by both client and server are defined once in `shared/commonMain` (e.g. `shared/src/commonMain/kotlin/com/frame/zero/dto/`). The server depends on `shared` for these. DTOs are organised by domain subdirectory (`common/`, `dashboard/`, `production/`, `schedule/`, `notification/`, `task/`).
- **Convention plugins** (`build-logic/`): `crossplatform.kmp.library`, `crossplatform.kmp.library.compose`, and `crossplatform.code.quality` configure targets, SDK versions, and code quality tooling. New KMP modules should apply one of the first two (they inherit code quality automatically).
- **Server DB migrations:** Flyway manages schema evolution. Migration files live in `server/src/main/resources/db/migration/` using the naming convention `V<N>__<description>.sql`.
- **Offline-first repositories:** Paginated lists use Room (KMP) + Paging 3 `RemoteMediator`. `shared/repositories/productions/` is the reference impl. The repo returns `Flow<PagingData<DomainType>>`; UI observes Room, never the network directly. Room modules apply `libs.plugins.ksp` + `libs.plugins.androidxRoom` and register `ksp` configs for all targets.
- **Sign-out cleanup:** Any module owning user-scoped local state (Room cache, in-memory user data) implements `SessionCleaner` (`shared/.../core/session/SessionCleaner.kt`) and registers in its Koin module with `single { … } bind SessionCleaner::class`. `SessionManager` invokes every registered cleaner — each under its own `runCatching` — before clearing tokens in `forceLogout()`, covering both user-initiated sign-out and the auto-logout path triggered by a failed refresh-token rotation. Reference impl: `ProductionsSessionCleaner` → `ProductionsDao.clearAll()`.

## Adding a New Feature

1. Create `shared/features/<name>/` with a `build.gradle.kts` applying `crossplatform.kmp.library`.
2. Create `composeApp/features/<name>/` with a `build.gradle.kts` applying `crossplatform.kmp.library.compose`.
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
- **Expect/Actual:** When adding one, implement both actuals (`androidMain`, `iosMain`) in the same change. The `shared` module also retains a `jvm()` target for `:server` consumption, so any `expect` declared there also needs a `jvmMain` actual.
- **Design system:** Use `AppTheme.colorSystem.*`, `AppTheme.spacingSystem.*`, `AppTheme.radiusSystem.*`, `AppTheme.typographySystem.*`. Never use `MaterialTheme` or hardcoded `Color`/`dp` values in feature UI. Use `spacingSystem` tokens only for spacing (padding, gaps) — never for size, width, or border-width values.
- **Spacers:** Use `VerticalSpacer(AppTheme.spacingSystem.spaceN)` and `HorizontalSpacer(AppTheme.spacingSystem.spaceN)` from the design system (`com.frame.zero.shared.design_system.widgets`). Never use a raw `Spacer(Modifier.height/width(...))`.
- **Clickable:** Use `Modifier.clickableWithRipple(color = ...)` from `com.frame.zero.shared.design_system.modifier` instead of the bare `Modifier.clickable { ... }` so every clickable surface gets a themed Material 3 ripple (`bounded`/`radius` configurable).
- **Previews:** Always use `@LightDarkPreview` (from `com.frame.zero.shared.design_system`) instead of a plain `@Preview`. This generates both light and dark previews automatically.
- **Errors:** Use `Outcome<T>` (sealed type in `shared/.../domain/Outcome.kt`) or `Result<T>` across layer boundaries; don't throw.
- **StateFlow updates:** Always use `_state.update { it.copy(...) }` instead of `_state.value = _state.value.copy(...)`. The `update` function is atomic and avoids race conditions with concurrent emissions.
- **Serialization:** `kotlinx.serialization` with `@Serializable` on shared DTOs.
- **Dependencies:** Always add to `gradle/libs.versions.toml`, never inline in build scripts.

## Things to Avoid

- No `kapt` — use `ksp`.
- No Java-only or Android-only libraries in `commonMain`.
- No `LocalContext.current` in shared Compose code.
- No duplicated request/response models between `server` and clients.
- No SwiftUI code unless explicitly requested.
- No SQLDelight or Realm — Room KMP is the local DB; `multiplatform-settings` for small k/v storage.

## Key File Locations

| What | Where |
|------|-------|
| Version catalog | `gradle/libs.versions.toml` |
| Convention plugins | `build-logic/src/main/kotlin/` |
| Domain models | `shared/src/commonMain/kotlin/com/frame/zero/domain/` |
| Shared DTOs | `shared/src/commonMain/kotlin/com/frame/zero/dto/` |
| Root navigation | `composeApp/src/commonMain/` (look for `RootComponent`) |
| Design system tokens | `composeApp/shared/design_system/` |
| Detekt config | `config/detekt/detekt.yml` |
| Flyway migrations | `server/src/main/resources/db/migration/` |

## Current Modules

**Features** (shared logic + compose UI):
`account`, `auth`, `home`, `production`, `production-details`, `task-details`

**Repositories** (`shared/repositories/<name>/`):
`auth`, `user`, `dashboard`, `productions`, `schedule`

**Server domains** (`server/src/main/kotlin/com/frame/zero/`):
`auth`, `dashboard`, `notification`, `production`, `schedule`, `task`

