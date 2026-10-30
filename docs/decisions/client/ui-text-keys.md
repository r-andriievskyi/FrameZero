# UiText = error keys

**Why:** shared ViewModels must carry user-facing error text without depending on any UI
toolkit, so the same state renders from Compose (Android) and SwiftUI (iOS). `UiText` holds an
`ErrorStringKey` enum (`shared/ui_text`); each platform resolves it with an exhaustive
`when`/`switch`, so a key missing from either catalog is a compile error, not a blank label.
Android's catalog is one Compose resource bundle in `composeApp/shared/ui_text`
(`UiTextExt.kt`, `values` + `values-es`).

**Not:**
- **Compose `StringResource` in `UiText`** — what this replaced. Pinned Compose (compiler,
  runtime, resources) into every `shared/features/*` module and the iOS binary, and Swift
  cannot resolve a `StringResource`.
- **Resolving on iOS via compose-resources' `getString`** — keeps compose-resources in the
  iOS binary forever, and resource bundling for a non-Compose host is fragile.

**Cost:** one central enum + catalog instead of per-feature bundles; feature copy is
namespaced by prefix (`TASK_DETAILS_NOT_FOUND`). No format args — nothing needed them; add a
field to `UiText.Resource` when something does.
