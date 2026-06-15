# Room (KMP) for offline-first lists

**Why:** paginated lists are **offline-first** — Room (KMP) is the local source
of truth and the UI observes it, never the network directly. A Paging 3
`RemoteMediator` writes API pages into Room (REFRESH replaces, APPEND
cursor-paginates), so lists survive flaky networks and cold starts.
`shared/repositories/productions` is the reference impl. Auth tokens still live
in `multiplatform-settings` — Room is for lists, not a few strings.

**Not:**
- **Network as sole source of truth** — where we started; flaky network = blank
  screens. Reversed once lists needed to persist.
- **SQLDelight** — KMP-native and type-safe, but Room brings first-class Paging
  3 integration (`RemoteMediator`, `PagingSource`) we'd otherwise hand-roll.
- **Realm Kotlin** — heavier runtime, vendor coupling.

**Cost:** Room's KMP builder is platform-specific — each db-owning module needs
`expect DatabaseBuilderFactory` with Android/iOS actuals wired through Koin.
Not in prod yet, so `exportSchema = false` and no migrations; flip both on
before shipping. Any module owning user-scoped Room data must implement
`SessionCleaner` so sign-out wipes it.
