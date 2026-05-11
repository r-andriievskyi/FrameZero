# JWT (access) + opaque refresh + bcrypt

**Why:** stateless access (15 min JWT) means no DB hit on the hot path —
scales horizontally with no shared session store. Opaque refresh tokens
(30 d) live in `refresh_tokens` so they're individually revocable.
Bcrypt for passwords: boring, well-supported.

**Not:**
- **Server-side sessions** — DB hit per request, sticky-session pain at
  instance #2.
- **Long-lived JWTs, no refresh** — revocation needs a server blocklist,
  defeats the point.
- **Auth0 / Firebase Auth** — vendor lock-in, premature.

**Cost:** revocation lag = access TTL (15 min). Acceptable. Auth routes
are rate-limited (10/min).
