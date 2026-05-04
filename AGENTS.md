# AGENTS.md

## Project Overview

FrameZero is a Kotlin Multiplatform (KMP) app targeting Android, iOS, Desktop, and a Ktor server backend. Navigation uses Decompose; DI uses Koin; UI uses Compose Multiplatform with a custom design system.

## Architecture — Key Decisions

- **Maximise Kotlin sharing.** Default to `commonMain`; use platform source sets only when no multiplatform API exists.
- **Decompose for navigation:** `RootComponent` in `composeApp/commonMain` owns a `StackNavigation`. Feature components live in `shared/features/<name>/`. Feature UI in `composeApp/features/<name>/` is stateless — all logic stays in `shared`.
- **Shared wire types:** DTOs and constants used by both client and server are defined once in `shared/commonMain` (e.g. `shared/src/commonMain/kotlin/com/frame/zero/dto/`). The server depends on `shared` for these.
- **Convention plugins** (`build-logic/`): `crossplatform.kmp.library` and `crossplatform.kmp.library.compose` configure all targets, SDK versions, and code quality tooling. New modules should apply one of these.

## Adding a New Feature

1. Create `shared/features/<name>/` with a `build.gradle.kts` applying `crossplatform.kmp.library`.
2. Create `composeApp/features/<name>/` with a `build.gradle.kts` applying `crossplatform.kmp.library.compose`.
3. Register both in `settings.gradle.kts` (e.g. `include(":shared:features:<name>")`, `include(":composeApp:features:<name>")`).
4. Add a Koin module (see `shared/features/auth/src/commonMain/.../AuthModule.kt` as reference).
5. Wire the new component into `RootComponent`'s `ChildStack`.

## Commands

```bash
./gradlew :composeApp:run              # Desktop (fastest iteration with hot reload)
./gradlew :server:run                  # Ktor server on port 8080
./gradlew ktfmtFormat                  # Format before committing (Google style, 2-space indent)
./gradlew check                        # Full CI gate: format check + detekt + tests
```

## Patterns to Follow

- **Koin DI:** Each feature exposes a `val <name>Module = module { ... }` (see `authModule` in `AuthModule.kt`). ViewModels are `factory`, repositories are `single`.
- **Decompose components:** Accept `ComponentContext` + ViewModel factories via constructor. Use sealed `Config`/`Child` interfaces for sub-navigation (see `AuthComponent.kt`).
- **Expect/Actual:** When adding one, implement all three actuals (`androidMain`, `iosMain`, `jvmMain`) in the same change.
- **Design system:** Use `AppTheme.colorSystem.*`, `AppTheme.spacingSystem.*`, `AppTheme.radiusSystem.*`, `AppTheme.typographySystem.*`. Never use `MaterialTheme` or hardcoded `Color`/`dp` values in feature UI.
- **Errors:** Use `Outcome<T>` (sealed type in `shared/.../domain/Outcome.kt`) or `Result<T>` across layer boundaries; don't throw.
- **Serialization:** `kotlinx.serialization` with `@Serializable` on shared DTOs.
- **Dependencies:** Always add to `gradle/libs.versions.toml`, never inline in build scripts.

## Things to Avoid

- No `kapt` — use `ksp`.
- No Java-only or Android-only libraries in `commonMain`.
- No `LocalContext.current` in shared Compose code.
- No duplicated request/response models between `server` and clients.
- No SwiftUI code unless explicitly requested.
- No Room/SQLDelight yet — `multiplatform-settings` covers current needs.

## Key File Locations

| What | Where |
|------|-------|
| Version catalog | `gradle/libs.versions.toml` |
| Convention plugins | `build-logic/src/main/` |
| Domain models | `shared/src/commonMain/kotlin/com/frame/zero/domain/` |
| Shared DTOs | `shared/src/commonMain/kotlin/com/frame/zero/dto/` |
| Root navigation | `composeApp/src/commonMain/` (look for `RootComponent`) |
| Design system tokens | `composeApp/shared/design_system/` |
| Detekt config | `config/detekt/detekt.yml` |

