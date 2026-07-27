# Security Review — Findings & Hardening Plan

Date: 2026-07-04 · **Re-verified 2026-07-24 against `chat_offline_send`** (file:line anchors
refreshed to HEAD; nothing in the plan below has landed). Scope: whole project (client + server),
**excluding the chat feature** — chat REST/WebSocket authorization, the chat Room cache, the offline
outbox and chat-screen protections are reviewed in [`chat-security-review.md`](chat-security-review.md).
Review was code-reading only; no dynamic testing.

## TL;DR

The security baseline is strong for an MVP: tokens are hardware-backed on both platforms, the
server does bcrypt + refresh-token rotation with theft detection, and there are real 401/403 route
tests. The remaining gaps here are (1) transport hardening (certificate pinning) since plain
HTTPS/WSS trusts every CA on the device, (2) at-rest protection for the Room cache, (3) an
unauthenticated `/metrics` endpoint plus error bodies fanning out to third-party log sinks, and
(4) no automated security testing in CI (dependency, secret, and authz-matrix coverage).

---

## 1. What is already done well (verified 2026-07-24, keep as-is)

| Area | Evidence |
|---|---|
| Token storage, Android | AES-256-GCM via Android Keystore over SharedPreferences (`SessionModule.android.kt`) — key never leaves hardware |
| Token storage, iOS | Keychain (`SessionModule.ios.kt`) |
| Password storage | bcrypt, hashed outside DB transaction (`AuthService.kt`) |
| Refresh tokens | Stored as SHA-256 hashes only; atomic claim + rotate; replay of a rotated token revokes **all** sessions for that user (`AuthService.refresh`) |
| JWT | HMAC-256 with issuer + audience verification; 15 min access TTL; prod boot fails without `JWT_SECRET` (`JwtConfig.fromEnv`) |
| SQL | Exposed typed DSL everywhere, `limit` coerced to `1..100`, no string-built SQL |
| Rate limiting | Per-IP on `/auth/*`, keyed on `origin.remoteHost` (`Application.kt:157`) |
| Input validation | email/name/password validated server-side; production/task field limits enforced in services |
| Android manifest | `allowBackup="false"`; cleartext permitted **only** for localhost/emulator hosts via `network_security_config.xml` |
| iOS transport | No ATS exceptions in `Info.plist` — cleartext blocked by default |
| File storage | Blob keys are server-generated UUIDs; `openStream` never touches user input — no path traversal |
| Secrets hygiene | `google-services.json` / `GoogleService-Info*.plist` gitignored; server secrets from env vars |
| Sign-out | `SessionCleaner` fan-out wipes user-scoped local state on logout; app lock (`core/security`) gates the signed-in session |
| Release build | R8 minification (`androidApp/build.gradle.kts:88`) + `proguard-android-optimize.txt` |

---

## 2. Findings (ordered by priority)

### F1 — MEDIUM · No certificate pinning (this answers "HTTPS/WSS might not be enough")

`androidApp/src/main/res/xml/network_security_config.xml`, `shared/.../network/NetworkModule.kt`

HTTPS/WSS gives you confidentiality against passive attackers, but the client trusts *every* CA in
the device trust store. A user-installed root CA (corporate proxy, malware, coerced user +
mitmproxy), or a compromised/rogue CA, lets an active MITM read and modify all traffic — including
tokens. Pinning closes that class. Status 2026-07-24: the config still declares only the cleartext
dev-domain block, no `<pin-set>`; no trust evaluation on the Darwin engine.

**Fix (no server change needed):**
- **Android:** add a `<pin-set>` to the existing `network_security_config.xml` for the prod domain
  (pin the SPKI hash of your leaf or intermediate, plus a backup pin). Declarative, applies to
  OkHttp/Ktor automatically.
- **iOS:** implement pinning in the Darwin Ktor engine via `handleChallenge` / `URLSession` trust
  evaluation against the same SPKI hashes (expect/actual in `HttpClientFactory` — consistent with
  the hand-rolled-over-deps preference).
- Pin the **SPKI of the key**, not the certificate, so normal cert renewal doesn't brick the app;
  always ship ≥ 2 pins (current + backup key); keep pins out of debug builds so local dev against
  `10.0.2.2` keeps working.
- WSS upgrade goes through the same engine, so pinning covers sockets too.

### F2 — MEDIUM · Room cache stored in plaintext on device

`shared/database/.../DatabaseModule.kt` — `BundledSQLiteDriver()`, no cipher driver.

Tokens are encrypted, but the shared Room DB (productions, tasks, schedule, and the chat tables
covered in the chat review) sits in an unencrypted SQLite file. Threat model check, honestly:

- Android FBE + app sandbox and iOS Data Protection already encrypt the file at rest; another app
  cannot read it on a non-rooted/non-jailbroken device.
- The residual risk is a rooted/jailbroken device, a lost device with a weak or no passcode, or
  forensic extraction.

**Options (pick one):**
1. **Accept the platform baseline** and document it (cheapest). Enable iOS Data Protection
   explicitly (`NSFileProtectionComplete` entitlement) so it's guaranteed, not incidental — as of
   2026-07-24 `iosApp/iosApp/Info.plist` declares no file-protection key.
2. **SQLCipher for Room** with the passphrase held in the Android Keystore / iOS Keychain,
   mirroring the existing token pattern. Cost: a native dependency and key-management code in the
   shared DB module.

Recommendation: option 1 now + explicit iOS file-protection entitlement; revisit option 2 if the
cache starts carrying genuinely sensitive content (see the chat review — message bodies already
push in that direction).

### F3 — LOW · `/metrics` endpoint is unauthenticated

`Application.kt:169` `installMetrics` → `get("/metrics")` in a bare `routing {}`, outside any
`authenticate` block. Prometheus scrape output — route names, timings, JVM internals — is
reconnaissance data. **Fix:** bind it to a separate internal port, require a bearer token, or
restrict it at the reverse proxy.

### F4 — LOW · Prod error-response bodies fan out to log sinks

`NetworkModule.kt:91-100` `HttpResponseValidator` logs `bodyAsText()` of every server error via
`appLogger.e`, which reaches Crashlytics in release builds. Today's error bodies are structured
`ErrorResponse`s, but a future endpoint echoing user input would leak it into a third-party
service. **Fix:** log status + request path only in release, full body only when `isDebug`.

### F5 — LOW · Screenshot / recents protection is opt-in

`MainActivity.kt:136-142` toggles `FLAG_SECURE` from `appLockController.enabled`, so app-switcher
and screenshot protection exists **only for users who turned on biometric app lock**. iOS has no
snapshot blurring on `willResignActive` at all. **Fix:** decide per-screen — force `FLAG_SECURE`
while any screen showing user content is on top, independent of app lock; blur the iOS snapshot.
(The chat-screen slice of this decision is tracked in the chat review.)

### F6 — INFO · `XForwardedHeaders` trusted unconditionally

`Application.kt:106`, already flagged in a code comment: if the listener is ever exposed without a
proxy, clients can spoof their IP and bypass the per-IP auth rate limit. Keep the deployment
invariant written down (proxy strips/sets `X-Forwarded-For`).

---

## 3. Security tests — current state and plan

**Have today (2026-07-24):** auth service/route tests incl. refresh rotation; token hasher /
password hasher / JWT service tests; scattered 401/403 route tests for tasks, productions,
schedule, notifications and dashboard; `FilesystemFileStorageTest`. No security-specific CI
tooling — `.github/workflows/ci.yml` runs ktlint + detekt only.

**Add (in priority order):**

1. **Authorization matrix tests (server, biggest value).** One parameterized suite that walks every
   authenticated route with three callers — no token, valid token but wrong user
   (non-member/non-owner), authorized user — and asserts 401/403/2xx. New routes get added to the
   matrix, so authz coverage can't silently regress. Generalize the existing per-route pattern to
   tasks / productions / schedule / dashboard / notifications / files.
2. **IDOR sweep.** For every resource fetched by UUID, assert another user's UUID returns 403/404
   rather than the row — productions, tasks, schedule events, attachments.
3. **Auth abuse paths.** Per-IP rate limit actually rejects after the configured burst; login does
   not distinguish "unknown email" from "wrong password" in status or timing-visible branching;
   refresh replay revokes the whole session family (extend the existing rotation test).
4. **Upload/file storage.** Attachment keys are server-generated and a caller-supplied key or
   traversal-shaped path never reaches `openStream`; download requires access to the owning
   resource.
5. **Transport regression guards.** Once F1 lands: pinned build rejects a MITM cert in an
   instrumented test; debug build still reaches `10.0.2.2`.
6. **CI tooling.** Add `dependabot.yml` (or Renovate) for dependency updates, `gitleaks` for secret
   scanning, and an OSS dependency-vulnerability check to `.github/workflows/ci.yml`. All three are
   cheap, run in parallel with the existing lint gate, and catch the classes of problem code review
   does not.

---

## 4. Plan (priority order)

| # | Item | Effort | Priority |
|---|------|--------|----------|
| 1 | F1: TLS pinning (Android `<pin-set>` + iOS trust evaluation) with backup pin | M | P1 |
| 2 | Tests 1–2: authz matrix + IDOR sweep | M | P1 |
| 3 | F4: strip error bodies from release logging | S | P1 |
| 4 | F2: iOS `NSFileProtectionComplete` entitlement (option 1) | S | P1 |
| 5 | Test 6: gitleaks + Dependabot + dependency-vuln scan in CI | S | P2 |
| 6 | F3: gate / internal-bind `/metrics` | S | P2 |
| 7 | Tests 3–4: auth abuse paths + file storage | M | P2 |
| 8 | F5: decide screenshot policy (Android `FLAG_SECURE` scope + iOS snapshot blur) | S | P2 |
| 9 | F2 option 2: SQLCipher-backed Room, if data sensitivity rises | M | P3 |
| 10 | F6: document the reverse-proxy deployment invariant | S | P3 |

P1 = before this handles real user data; P2 = hardening backlog; P3 = revisit when scope changes.
Chat-specific items (WS revocation on member removal, WS session TTL, chat cache encryption,
chat-screen `FLAG_SECURE`) are prioritized separately in [`chat-security-review.md`](chat-security-review.md).
