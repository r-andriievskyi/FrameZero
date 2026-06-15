# expect / actual for platform code

**Why:** when an API genuinely doesn't exist in `commonMain` (HTTP engine
choice, secure storage, Room builder), declare `expect` once and provide an
`actual` on Android and iOS. Keeps platform branching out of business logic.
We target **android + ios only** — no `jvm()` target anywhere — so an `expect`
needs exactly those two actuals, no more.

**Not:**
- **`if (Platform.isAndroid)` runtime checks** — no compile-time
  guarantee every platform is handled, and pulls platform deps into
  `commonMain`.
- **Separate per-platform classes referenced manually** — same problem
  without the type system's help.

**Cost:** every `expect` needs both actuals shipped together. iOS gets
forgotten most often (Android-first muscle memory) — the rule is "ship Android
and iOS in the same change."
