# multiplatform-settings (token storage)

**Why:** tiny key/value API that wraps `SharedPreferences` on Android and
`NSUserDefaults` on iOS. Right size for the small stuff: auth tokens and prefs.

**Not:**
- **Room** — that's our offline-first list store
  ([room-offline-first](room-offline-first.md)); reaching for it to hold a few
  strings is overkill. Different tool, different job.
- **DataStore (Android only)** — not multiplatform.
- **Per-platform `expect/actual` prefs** — reinventing this library.

**Cost:** not encrypted by default. When the threat model demands it,
wrap with platform keystores via `expect/actual` rather than swap libs.
