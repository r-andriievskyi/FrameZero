# multiplatform-settings (token storage)

**Why:** tiny key/value API that wraps `SharedPreferences` on Android,
`NSUserDefaults` on iOS, properties files on JVM, `localStorage` on Web.
Right size for what we store today: auth tokens.

**Not:**
- **Room / SQLDelight** — overkill for a few strings; see
  `no-local-db-yet`.
- **DataStore (Android only)** — not multiplatform.
- **Per-platform `expect/actual` prefs** — reinventing this library.

**Cost:** not encrypted by default. When the threat model demands it,
wrap with platform keystores via `expect/actual` rather than swap libs.
