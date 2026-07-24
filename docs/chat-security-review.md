# Chat MVP — Security Review & Hardening Plan

Original branch: `feature/chat-mvp-client`, since merged (`#9` backend, `#10` client MVP, `#11` read markers). Scope: chat feature (server REST + WebSocket, client repo + Room cache + WS client, Compose UI), plus a project-wide look at data-at-rest and data-in-transit per the review request.

**Re-verified 2026-07-24 against `chat_offline_send`: F1–F5 all still open, nothing in the plan table done.** The offline-outbox work widened F2 (see below) and partially landed plan item 8. File:line anchors below refreshed to current HEAD.

## TL;DR

The chat PR is **well-built on the security basics**: server-side authorization is enforced on every REST and WebSocket path, queries are parameterized, tokens live in Keystore/Keychain, and there is real authz test coverage. **One concrete authorization bug** was found (post-removal message leak over a live socket). The rest of the list is **defense-in-depth** for the "cannot be breached or stolen" bar you asked about — HTTPS/WSS alone is not enough, and the items below close the gaps HTTPS does not cover.

---

## Findings

### F1 — Authorization bypass: removed production member keeps receiving chat over live socket
- **Severity:** Medium · **Confidence:** High
- **Where:** `server/.../production/ProductionService.kt:242` `removeMember` vs `server/.../chat/ChatHub.kt:56` `broadcast`
- **What:** WS authorization is checked once at `SUBSCRIBE` and then cached as a per-socket subscription set. A revocation hook exists for task-circle changes (`ChatService.kt:189` → `ChatHub.retainSubscribers`), but **`ProductionService.removeMember` fires no revocation** — the service does not even hold a `ChatHub` (constructor, `ProductionService.kt:29-36`). `install(WebSockets)` (`Application.kt:121`) sets no idle/ping timeout, so the socket lives indefinitely; the JWT staying valid does not bound it either. Every message posted after removal is fanned out to the removed user in real time.
- **Exploit:** Authorized user subscribes to a task conversation, admin removes them from the production (all REST chat access correctly denied), but the existing subscription keeps delivering new messages — including discussion of their removal — for as long as a trivial script keeps the socket open.
- **Fix:** On `ProductionService.removeMember`, after commit, drop that user's hub subscriptions / close their sockets for every conversation in the production (e.g. `ChatHub.dropUser(userId, productionId)` fed by `ConversationsTable.productionId`). Defense in depth: cheap periodic re-check of `canAccessConversation`, and a WS idle timeout.

### F2 — Chat messages cached unencrypted on device (data at rest)
- **Severity:** Medium · **Confidence:** High
- **Where:** `shared/database/.../DatabaseModule.kt:18` — `BundledSQLiteDriver()`, no SQLCipher; `MessageEntity`/`ConversationEntity` store bodies in plaintext, and since the offline-send work so does `PendingMessageEntity` (`chat_pending_messages.body`).
- **What:** Tokens are encrypted (Keystore/Keychain), but the Room DB holding **message bodies** is plaintext on disk. On a rooted/jailbroken device, a compromised backup path, or forensic extraction, chat contents are readable. `allowBackup=false` helps on Android; iOS relies on file protection only.
- **Widened by the offline outbox:** queued-but-unsent bodies now also persist in `chat_pending_messages` and can sit there across retries/backoff for a long time. Sign-out is clean (`ChatSessionCleaner` wipes pending rows — see Clean), so this is strictly an at-rest exposure, not a cross-user leak.
- **Fix:** Encrypt the DB at rest — SQLCipher with a Keystore/Keychain-held key (`androidx.sqlite` supports a cipher driver), or scope the passphrase to the same secure store used for tokens. At minimum, set iOS `NSFileProtectionComplete` on the DB file and confirm Android `allowBackup=false` (already set) plus no debuggable release.

### F3 — No TLS certificate/public-key pinning (data in transit)
- **Severity:** Medium · **Confidence:** High
- **Where:** `shared/.../network/NetworkModule.kt`, `ChatSocketClient.kt` (both rely on platform trust store only). `androidApp/src/main/res/xml/network_security_config.xml` declares only a cleartext dev-domain block — no `<pin-set>`.
- **What:** HTTPS/WSS protects against passive eavesdropping but **not** an attacker with a trusted/mis-issued CA cert, a user-installed root, or a corporate MITM proxy — exactly the "data could be stolen in transit" case. Without pinning, a MITM with a valid-looking cert reads and modifies all traffic, including tokens and messages.
- **Fix:** Pin the server certificate or SPKI. Android: OkHttp `CertificatePinner` (Ktor OkHttp engine) or `network_security_config` `<pin-set>`. iOS: `URLSessionDelegate` trust evaluation on the Darwin engine. Ship a backup pin and a rotation plan.

### F4 — WS subscription outlives the 15-minute access token
- **Severity:** Low · **Confidence:** Medium
- **Where:** `server/.../chat/ChatRoutes.kt:78` `chatWebSocket`, `Application.kt:121` bare `install(WebSockets)`.
- **What:** JWT is verified only on the upgrade request. Access tokens expire in 15 min, but a subscribed socket keeps delivering messages long after — same root shape as F1. A revoked/expired session has no in-band way to be evicted.
- **Fix:** Add a WS idle/max-lifetime timeout and periodic re-authorization; force clients to reconnect (which re-runs `auth-jwt`) on a bounded cadence.

### F5 — `/metrics` endpoint is unauthenticated
- **Severity:** Low · **Confidence:** Medium
- **Where:** `server/.../Application.kt:169` `installMetrics` → `get("/metrics")` in a bare `routing {}`, outside any `authenticate` block.
- **What:** Prometheus scrape is public. Not chat-specific and may be intended for an internal-only network, but if the listener is reachable it leaks operational detail (route names, volumes). Flagging as pre-existing, not introduced by this PR.
- **Fix:** Bind `/metrics` to an internal interface, or gate behind auth / network policy.

---

## Clean — verified, no action needed
- **REST authz:** `getOrCreateTaskConversation`, `listMessages`, `send` all call `requireCircle` / `authorizeConversation`; no IDOR, participant rows are read-state only, never used as an ACL.
- **WS auth:** `/ws` inside `authenticate("auth-jwt")`; token in header not URL; every `SUBSCRIBE` re-checks the DB; subscription cap enforced.
- **SQL injection:** Exposed typed DSL throughout; `before`/`limit` parsed to numbers, `limit` coerced to 1..100.
- **Deserialization:** sealed `ChatSocketFrame`, `ignoreUnknownKeys`, parse failures swallowed — no polymorphic gadget instantiation.
- **Logging:** no message bodies, tokens, or user ids logged in the new code.
- **Idempotency:** `client_message_id` unique per `(conversation, sender, id)` — no cross-user collision.
- **Sign-out:** `ChatSessionCleaner` stops the socket and clears the Room cache.
- **Offline outbox sign-out cleanup:** `ChatSessionCleaner.clear()` cancels scheduled work, stops and *awaits* in-flight delivery, then wipes pending rows (`ChatDao.clearAll()` → `deleteAllPendingMessages()`). No in-flight send can land a signed-out user's body in the next user's DB. No message bodies logged in the new outbox code.
- **Token storage:** Android Keystore AES-256-GCM over prefs; iOS Keychain.
- **Server auth core:** bcrypt, refresh-token rotation with theft detection, per-IP auth rate limit, JWT issuer+audience verified.

---

## Security test gaps

Existing chat tests cover 401 (no token) and 403 (not in circle) on `GET conversation`, and the service `Forbidden` path. Add:

1. **F1 regression:** removed production member no longer receives broadcasts on an already-subscribed socket. (Integration test against `ChatHub` + `ProductionService.removeMember`.)
2. **WS subscribe authz:** `SUBSCRIBE` to a conversation the caller is not in the circle of is silently dropped (no delivery).
3. **IDOR sweep:** `listMessages` / `send` / `getOrCreateTaskConversation` with another user's conversation UUID → 403/404 for each.
4. **Idempotency isolation:** two users reusing the same `client_message_id` produce two distinct messages.
5. **Message validation:** empty body and body > 4000 chars rejected; oversized `client_message_id` rejected.
6. **Token-not-in-URL:** assert the WS upgrade carries the bearer in the header and the URL has no query token (guards against a future refactor that logs it).
7. **Outbox does not resend after sign-out:** a queued message plus a concurrent `ChatSessionCleaner.clear()` produces no send — regression guard for the cancel → await → wipe ordering.

Note: body > 4000 chars and oversized `client_message_id` are validated in `ChatService.kt:125-134` but only the blank-body path has a test — gap 5 is about covering the enforced limits, not adding them.

Tooling: no dependency/secret scanning in CI. Add `dependabot.yml` (or Renovate), a secret scanner (gitleaks), and an OSS dependency-vuln check to `.github/workflows/ci.yml`.

---

## Plan (priority order)

| # | Item | Effort | Priority |
|---|------|--------|----------|
| 1 | F1: revoke hub subscriptions on `removeMember` + regression test | M | P0 |
| 2 | F4: WS idle timeout + periodic re-auth | S | P1 |
| 3 | F2: encrypt Room DB at rest (SQLCipher + Keystore/Keychain key) | M | P1 |
| 4 | F3: TLS pinning (Android + iOS) with backup pin | M | P1 |
| 5 | Security tests 1–6 above | M | P1 |
| 6 | CI: gitleaks + Dependabot + dependency-vuln scan | S | P2 |
| 7 | F5: gate/internal-bind `/metrics` | S | P2 |
| 8 | `FLAG_SECURE` on chat screens — partially landed, see below | S | P2 |

P0 = ship-blocker for the chat feature; P1 = before this handles real user data; P2 = hardening backlog.

Item 8 status: `MainActivity.kt:136-142` toggles `FLAG_SECURE` with `appLockController.enabled`, so screenshot/recents protection exists **only for users who turned on biometric app lock**. Chat screens are unprotected by default. Open decision: force `FLAG_SECURE` whenever a chat screen is on top, independent of app lock.
