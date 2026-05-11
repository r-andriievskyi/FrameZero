# No local database (yet)

**Why:** a real DB (Room KMP / SQLDelight) is a non-trivial addition —
schema, migrations, threading, cache invalidation. Today the client
stores only auth tokens; the network is the source of truth. YAGNI
until offline-first or large local lists demand it.

**Not (when the time comes):**
- **SQLDelight** — KMP-native, type-safe SQL, well-trodden.
- **Room KMP** — newer, Google-backed; viable once it stabilises on iOS.
- **Realm Kotlin** — heavier runtime, vendor coupling.

**Cost:** flaky network = bad UX. When that becomes the top complaint,
pick one of the above — confirm with the owner before adding.
