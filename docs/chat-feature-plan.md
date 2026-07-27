# Chat feature plan

Realtime chat over WebSockets, built primarily as a system-design exercise. MVP is a
group chat per task; the data model is generic so 1:1 DMs and a production-wide channel
become later phases on the same protocol instead of rewrites.

**Status:** Phases 0-4 shipped (task participants, backend MVP, client MVP, unread/read
markers — PRs #8-#11; offline sends). Remaining phases below (5-8) not started.

## Design decisions

- **Task-scoped group chats, not production-wide.** A production can have 1k+ members, so
  a production channel means large fan-out, huge participant sets, and noisy unread state.
  A task thread has a naturally small audience (assignee, creator, a few interested
  members) while still exercising real group-chat semantics. The production-wide channel
  is deferred to Phase 7, where its fan-out problem becomes the explicit design exercise.
- **Generic data model from day one.** `conversations(kind)` / `conversation_participants` /
  `messages`. MVP only creates `TASK`-kind conversations (one per task, get-or-create).
  DMs and a production channel later are "a new kind", not a schema rework.
- **Chat access = the task's circle, and tasks grow a `participants` field (Phase 0).**
  Today a task has only a creator and one assignee, which would cap chat at 1:1. Phase 0
  adds `task_participants` (many-to-many) while keeping `assignee_user_id` as the single
  responsible person — e.g. a VFX supervisor is the assignee and their team are the
  participants. Chat access is creator + assignee + task participants, **not** all
  production members: the task circle is the security boundary. A production-membership
  check stays underneath as defense in depth.
- **Solo chats are valid; late joiners see full history.** A task with no assignee or
  participants yet still has a working chat (creator talking to themself / leaving
  notes). Anyone added to the task later sees the entire history — Slack-channel
  semantics, not WhatsApp-join semantics. This is a deliberate product choice with a
  security consequence: it requires server-readable history, which rules out
  Signal-style E2EE (see Security posture below).
- **`conversation_participants` is read-state, not ACL.** Rows are created lazily on
  first interaction (subscribe, send, mark-read) and carry `last_read_seq`. Authorization
  is always evaluated against the task circle at request time, so removing someone from
  the task revokes chat access immediately regardless of stale participant rows.
- **Write path: REST `POST` persists, WS broadcasts.** The socket is receive-only in the
  MVP. Sends get idempotency via a client-generated `clientMessageId`, reuse the existing
  JWT/refresh/`Outcome` machinery, and a dead socket never loses a write. A bidirectional
  ACK protocol is a deliberate later phase (see Phase 4), not skipped.
- **Ordering: server-assigned monotonic `seq` per conversation**, not timestamps. A
  per-conversation counter bumped inside the insert transaction. This is the load-bearing
  choice: gap detection on reconnect becomes "give me everything after seq N", and read
  markers / unread counts become exact.
- **Offline: UI observes Room** (same shape as `shared/repositories/productions`); REST
  backfills history, WS appends the live tail. Sends went through the persistent outbox in
  Phase 4, so they no longer require connectivity.
- **Text-only messages in the MVP, but a forward-compatible content model.** Emoji in
  text is free (UTF-8); everything else (reactions, images, files, audio) is Phase 8.
  What the MVP must get right so Phase 8 is additive, not a migration:
  - `ChatMessageDto` carries an optional `attachments` list (empty in MVP) rather than a
    bare `body`-only shape, so old clients tolerate new payloads.
  - Clients ignore unknown WS frame `type`s instead of failing — new event kinds
    (REACTION, etc.) must not break deployed apps.
  - Attachments later reuse the existing blob story (`task_attachments` +
    `FILE_STORAGE_DIR`): upload via REST first, then send a message referencing the
    attachment ids — which is another reason the REST write path pays off.

## Security posture

Chat content is the most sensitive data the app will hold (production-internal
discussions), so it gets the same treatment popular messengers apply — minus E2EE, which
the "late joiners see full history" requirement deliberately trades away (Slack's model,
not Signal's; per-conversation key redistribution for joiners is a good interview topic
but out of scope).

- **Transport:** `wss://` only (TLS), same as the REST base URL. No plaintext fallback.
- **Socket auth:** JWT in the `Authorization` header on the WS upgrade (mobile Ktor
  client can set headers — never the token in the URL query, where it lands in access
  logs). On token expiry the server closes with a policy code and the client
  refresh-then-reconnects.
- **Authorization on every operation:** task-circle check on `SUBSCRIBE`, on every REST
  read/write, and re-checked from the DB — never cached in the socket session — so a
  removed member is cut off mid-connection at the next operation. Subscriptions of a
  removed user are dropped on membership change (the hub gets told).
- **Push payload minimization (Phase 6):** data-only FCM messages carrying conversation
  and message ids, never the message body — content must not sit in FCM/APNs
  infrastructure or on the lock screen. The device fetches content and builds the
  notification locally (fall back to "New message" when fetch fails).
- **Logging hygiene:** message bodies never reach logs, `LogSink`/Crashlytics, or
  Analytics — log ids and sizes only. Same rule server-side (`CallLogging` must not
  capture chat bodies).
- **Abuse limits:** reuse `ktor-server-rate-limit` on the send route; enforce a max
  message length server-side; cap subscriptions per socket.
- **At-rest on device:** messages live in Room like other cached data; `SessionCleaner`
  wipes them on sign-out. Full DB encryption (SQLCipher / iOS Data Protection classes)
  is noted as a hardening option, decided app-wide rather than per-feature.
- **Attachments (Phase 8):** downloads go through an authenticated, task-circle-checked
  endpoint — no unauthenticated blob URLs.

## Phases 0-3 — shipped

Task participants, backend MVP, client MVP, unread counts/read markers. PRs #8-#11.
Ordering ended up as `ordinal` (not `seq` as originally drafted here).

## Phase 4 — Offline sends (outbox) — shipped

Client-only, as drafted: server idempotency from Phase 1 already carried it, so no wire or
schema change. `chat_pending_messages` (Room) plus a `ChatOutbox` drain, deliberately *not*
a reuse of the generic `core/upload` pattern — that scheduler (WorkManager per-upload unique
work names on Android, independent `NSURLSessionUploadTask`s on iOS) has no FIFO guarantee,
and out-of-order retries would scramble the server-assigned ordinal (arrival order, not
authorship order — see `MessageRepositoryImpl.append`).

How it ended up working:

- **Serial drain per conversation.** Send, await the response, only then take the next.
  Enforced twice over: a per-conversation `Mutex` in process, and a compare-and-set claim in
  SQL (`Queued → Sending`) so no second drain can take the same row.
- **Queue order is a monotonic `sequence` column**, never the wall clock — that ties on
  same-millisecond sends and moves backwards on an NTP correction. Local uniqueness is
  `(conversationId, clientMessageId)`, matching the server's
  `(conversation_id, sender_user_id, client_message_id)`.
- **Failure handling splits transient from permanent.** No network requeues *without*
  spending an attempt (the connectivity trigger re-kicks); a 5xx counts an attempt and stops
  so the message keeps its place; anything permanent — or a spent budget — parks the message
  as `Failed` and the queue keeps draining past it. A parked message only moves again by user
  retry or discard.
- **Recovery from interrupted sends** happens per conversation at the top of each drain,
  under its lock: a row left `Sending` can only be one a killed or cancelled drain stranded,
  so returning it to the queue is safe and a resend is deduped server-side.
- **Triggers:** enqueue, connectivity regain, socket reconnect, and an app-start flush
  (`ChatOutboxStarter`, eager Koin singleton gated on a signed-in session). Android adds a
  WorkManager backstop, one unique chain per conversation, so a queued message flushes after
  the app is killed. iOS has none — `BGTaskScheduler` timing is best-effort, so it relies on
  the in-process drain plus the app-start flush; a repo-side backoff loop covers a 5xx while
  the app is alive.
- **Optimistic bubbles** render after every confirmed message, dimmed while queued, with
  retry/discard actions once failed. The canonical copy retires the outbox row in the same
  transaction that lands it, so the list never flickers a duplicate.
- **Discard is best-effort:** a message already handed to the network still lands, the same
  way a delivered message can't be recalled.

Known limitation: the outbox keys on `conversationId`, so composing offline requires the chat
to have been opened at least once (`OpenConversationUseCase` falls back to the cached
conversation). A never-opened chat still shows the full-screen error offline.

Deferred out of this phase: promoting the write path to bidirectional WS with ACK frames. The
outbox makes it safe, and it's the classic ack/nack/resend protocol exercise — but it needs
server-side frame handling and leaves two write paths to keep in sync, so it earns its own
phase rather than riding along here.

## Phase 5 — Ephemeral events: typing + presence

- **Backend:** `TYPING`/`PRESENCE` frames routed through the hub, never persisted;
  presence derived from the socket registry with a grace timeout.
- **Client:** throttled typing emission (~3 s), "X is typing…" row, online dots. First
  client→server WS frames beyond `SUBSCRIBE`.

## Phase 6 — Push for offline recipients

- **Backend:** on broadcast, thread participants with no live socket get FCM via the
  existing `PushSender` (`TaskAssignmentNotifier` pattern). Lazy participants make the
  recipient set well-defined: only people who joined the thread get pushed. Data-only
  payload (conversation + message ids, no body — see Security posture). Design topics:
  the connection registry as the presence source; collapse keys / notification
  coalescing.
- **Client:** fetch content and build the notification locally; deep link from the push
  into the conversation.

## Phase 7 — More conversation kinds (DMs and/or production channel)

- **DMs:** new `DIRECT` kind + `GET /users/{id}/dm` get-or-create + a conversation list
  screen; reuses the entire repo/socket/UI stack.
- **Production-wide channel:** the deliberate scale exercise deferred from the MVP. At
  1k+ members the MVP assumptions break on purpose: no materialized participant rows
  (ACL-only access, read markers stored sparsely), fan-out moves from "loop over
  subscriber sockets" to a pub/sub layer (Redis) so multiple server nodes can serve one
  channel, and push must be digest/coalesced rather than per-message. Designing that
  delta is the point of the phase.

## Phase 8 — Rich content: reactions + attachments (images, files, audio)

- **Reactions:** `message_reactions (message_id, user_id, emoji)` table, toggled via
  REST, broadcast as `REACTION` WS frames (a message-mutation event, like Phase 4's
  edit/delete if that lands first). Aggregated counts render under the bubble.
- **Attachments:** two-step send — upload the blob over REST multipart (reusing the
  `task_attachments` + `FILE_STORAGE_DIR` machinery), then `POST` the message with
  attachment ids; the WS frame carries attachment metadata (kind, size, dimensions,
  duration), never bytes. New `message_attachments` table mirrors `task_attachments`.
- **Client:** attachment picker (hand-rolled expect/actual, as with the existing file
  picker), image thumbnails + full-screen viewer, audio record/playback last (most
  platform-specific surface). Outbox (Phase 4) must learn multi-step sends: blob upload
  completes before the message enqueues.
- Design topics: content-type allowlists and size limits, download caching keyed by
  attachment id, and why bytes never travel over the socket.

---

Phases 0-4 shipped. Phases 5-8 are independent enough to reorder.
