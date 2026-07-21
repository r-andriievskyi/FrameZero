# Force Update

Version gate for Android + iOS. Blocks builds below a minimum (**hard**) and nudges builds below
the latest (**soft**). Config source is **Firebase Remote Config** today — swappable to our backend
later via one interface.

## Flow

```
RootComponent.updateState  (overlay, mirrors biometric lock)
  → AppUpdateController     (gating + presentation timing)
    → CheckAppUpdateUseCase (compare build vs policy)
      → AppUpdateRepository  ← THE SEAM (RC now, backend later)
```

- `AppUpdateRepository.fetchPolicy()` → `UpdatePolicy(minSupportedBuild, latestBuild, storeUrl, message, critical)`.
  RC impl reads per-platform keys (`min_supported_build_android/ios`, …); backend swap = second impl + flip Koin binding.
- `AppVersionProvider` reads the **real shipped** build (Android `versionCode` / iOS `CFBundleVersion`) — not BuildKonfig.
- `CheckAppUpdateUseCase`: `current < min → HARD`, `< latest → SOFT`, else `NONE`.
- Gate compares **client-side** on integer build number, so RC/backend stays dumb (no conditions).

## Presentation (AppUpdateController)

State = f(resolved policy, softDismissed, metered), re-derived on any change.

- **Hard** always shows; back button swallowed; no dismiss.
- **Soft** non-`critical` on a **metered** (cellular) connection is deferred — resurfaces on wifi
  (observed live) or next refresh. `critical` soft never deferred.
- **Soft** dismissed stays hidden for the process (resurfaces next cold start).

Checked on cold start + app resume.

## Fail-open

Any failure resolves to `NONE` — never a false lockout:
- iOS unknown build → `Int.MAX_VALUE` (above any minimum).
- RC fetch fails / missing keys → Firebase static defaults (0/""/false) → `NONE`.
- Repository throws → `Outcome.Failure` → `NONE`.
- Demo flavor → `DemoAppUpdateRepository` returns all-zero → always `NONE`.

## Remote Config schema

Set in the Firebase console (Remote Config). Per-platform build/store keys avoid RC conditions;
`message`/`critical` are shared. Unset key → Firebase static default (`0`/`""`/`false`) → `NONE`.

| Key | Type | Example | Meaning |
|-----|------|---------|---------|
| `min_supported_build_android` | Number | `142` | Below → **HARD** (Android) |
| `latest_build_android` | Number | `150` | Below (≥ min) → **SOFT** (Android) |
| `store_url_android` | String | `https://play.google.com/store/apps/details?id=com.frame.zero` | Opened on Update |
| `min_supported_build_ios` | Number | `142` | Below → **HARD** (iOS) |
| `latest_build_ios` | Number | `150` | Below (≥ min) → **SOFT** (iOS) |
| `store_url_ios` | String | `https://apps.apple.com/app/id<APP_ID>` | Opened on Update |
| `update_message` | String | `Update for the new scheduler.` | Optional copy; blank → localized default |
| `update_critical` | Boolean | `false` | `true` = soft never deferred off cellular |

Build numbers = integer store builds (Android `versionCode` / iOS `CFBundleVersion`), compared
client-side. `market://details?id=…` also works for the Android URL.

## Why Remote Config

$0, console **is** the admin panel (nothing to build), native version/platform/percentage targeting,
gitlive SDK already wired. The seam keeps a backend swap cheap if we outgrow it.
