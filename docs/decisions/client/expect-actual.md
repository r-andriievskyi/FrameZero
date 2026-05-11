# expect / actual for platform code

**Why:** when an API genuinely doesn't exist in `commonMain` (HTTP engine
choice, secure storage, app context), declare `expect` once and provide
`actual` on Android, iOS, JVM. Keeps platform branching out of business
logic.

**Not:**
- **`if (Platform.isAndroid)` runtime checks** — no compile-time
  guarantee every platform is handled, and pulls platform deps into
  `commonMain`.
- **Separate per-platform classes referenced manually** — same problem
  without the type system's help.

**Cost:** every `expect` needs all three actuals shipped together.
Desktop and iOS get forgotten most often — the rule is "ship all three
in the same change."
