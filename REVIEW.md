# FrameZero Review Charter

Project-specific review criteria. Applies on top of the generic review pipeline. Full conventions live in `CLAUDE.md`; this file lists what a reviewer must actively check and what to skip.

## Noise filter — do NOT flag

CI already gates these; flagging them is noise:

- Formatting / style caught by ktlint or detekt (`lint` CI job).
- Design-system violations caught by the custom `:detekt-rules` ruleset (hardcoded colors, `MaterialTheme.*`, raw `dp`/`sp`, border/radius tokens).
- Test coverage thresholds (kover) and screenshot diffs (Roborazzi).

Focus on correctness, security, architecture invariants, and cross-file drift that per-file static analysis cannot see.

## Cross-file sync invariants (highest value — check every PR)

- **Wire DTOs are duplicated, not shared.** Any change to a wire type must touch BOTH the client copy (`shared/dto`, `com.frame.zero.dto.*` / `auth.dto.*`) and the server copy (same original package names under `server/`). A one-sided wire change is a blocking finding.
- **Server schema:** a Flyway migration (`server/**/db/migration/V<n>__*.sql`) and the matching Exposed `Table` object must change in the same PR. Never edit past migrations.
- **expect/actual:** a new `expect` in `commonMain` needs `androidMain` AND `iosMain` actuals in the same PR. No TODO/stub actuals, no runtime OS branching instead of actuals.
- **New feature module:** requires both halves (`shared/features/<name>` + `composeApp/features/<name>`), a Koin module registered in `composeApp/.../di/AppModule.kt`, entries in `settings.gradle.kts`, and `RootComponent` wiring.
- **Previews ↔ goldens:** adding/removing `@Preview`/`@LightDarkPreview` changes the Roborazzi golden set — goldens must be re-recorded in the same PR.

## Architecture invariants

- Layering: ViewModel → `UseCase`/`NoParamsUseCase` → repository. No repository calls from ViewModels; no re-wrapping use cases in try/catch; `Outcome<T>` across module boundaries, never thrown exceptions.
- MVI: UI talks to a ViewModel through ONE `onIntent(intent: <Feature>Intent)` channel (sealed intents), never discrete `doX()` methods. One-shot effects via sealed `events: SharedFlow`. Components are thin passthroughs — they must not read `viewModel.state.value` to make decisions.
- State: `_state.update { it.copy(...) }`, never `_state.value = …`.
- Strings: shared ViewModels emit `UiText`; never resolve resources to `String` in `shared`.
- Repository contracts speak domain types only (including command types like `NewTask`); DTO↔domain mapping lives inside `:impl` modules, never in use cases/ViewModels.
- Mappers do structural mapping only; UI hints (colors, badges) are domain-enum extension functions; formatting/labels belong in the ViewModel.
- Offline-first references: lists → `shared/repositories/productions` (Room + Paging 3 RemoteMediator); queued writes → `task-create` (`core/upload/`).

## Placement rules

- `commonMain` first; platform source sets only when no multiplatform API exists.
- Never in shared code: `java.time.*` (use kotlinx-datetime), kapt (use ksp), Java/Android-only libs in `commonMain`, `LocalContext.current` in `composeApp/commonMain`.
- Dependencies go in `gradle/libs.versions.toml` (prefer existing `[bundles]`), never inline in module scripts.
- `composeApp` UI: `AppTheme` tokens, `VerticalSpacer`/`HorizontalSpacer`, `Modifier.clickableWithRipple` — extracted composables `internal`, one per file, with previews.
- No Koin injection in `composeApp/commonMain` component constructors — inject component factories.

## Security

- Every new/changed server route needs an authorization check (production membership / task ownership), not just authentication.
- Membership/role revocation must propagate to live WebSocket sessions (removed member must not keep receiving chat).
- New sensitive data cached in Room is plaintext on device — call it out explicitly if introduced.
- No secrets, tokens, or raw response bodies in logs, analytics, or Crashlytics.
- New server input must be validated and length-bounded before persistence.
- Modules holding user-scoped local state must implement and bind `SessionCleaner`.

## Severity

- **Blocking:** correctness bugs, security issues, any cross-file sync invariant above.
- **Nit:** convention drift already documented in `CLAUDE.md` (naming, idiom, token usage not caught by detekt).
- When uncertain whether something is intentional, ask as a question rather than asserting a defect.
