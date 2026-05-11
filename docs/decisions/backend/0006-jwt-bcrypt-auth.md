# 0006 — Stateless JWT auth with bcrypt password hashing

- **Status:** Accepted
- **Date:** 2026-05-11

## Context

Clients are mobile (Android, iOS), desktop, and web. They need to
authenticate, stay authenticated across app restarts, and recover gracefully
when a session is revoked. The server is a single Ktor instance today but
must not assume that forever; horizontal scale should not require a shared
session store.

Password storage has to resist offline brute force in the event of a database
leak.

## Decision

- **Short-lived JWT access tokens** (15 minutes) signed with HS256, verified
  by Ktor's `jwt` authentication plugin.
- **Opaque refresh tokens** (30 days) stored server-side in the
  `refresh_tokens` table, rotated on use, individually revocable.
- **bcrypt** for password hashing via `at.favre.lib:bcrypt`.

Token TTLs and JWT claims (`issuer`, `audience`, `realm`) are configured in
`JwtConfig`; the signing secret is required from the environment in
production.

## Alternatives

- **Server-side sessions (cookie + session table)** — simple, but couples
  every authenticated request to a DB lookup and pushes us toward sticky
  sessions or a shared cache the moment we add a second instance.
- **Long-lived JWT access tokens, no refresh** — revocation becomes
  effectively impossible without a server-side blocklist, which defeats the
  point of stateless tokens.
- **Argon2 / scrypt** for password hashing — both are reasonable; bcrypt is
  the most boring, best-supported, and adequate at the cost factor we use.
  We will revisit if NIST guidance changes.
- **OAuth via a third party (Auth0, Firebase Auth, Cognito)** — would offload
  the auth surface but couple the product to a vendor's user model and
  pricing. Premature for the current scale.

## Consequences

- Authenticated requests are validated entirely from the JWT signature; no DB
  hit on the hot path.
- Revocation works at refresh time: deleting a row from `refresh_tokens`
  forces re-login within the access-token TTL (15 minutes). We accept this
  window as the cost of stateless access.
- The auth-related endpoints (`/auth/*`) are rate-limited (`AUTH_RATE_LIMIT
  = 10/min`) to blunt credential-stuffing.
- The signing secret is a deploy-time concern; the server refuses to start
  in production without `JWT_SECRET` set.
- Refresh tokens are opaque (not JWTs) so they can be rotated and revoked
  without re-issuing a new key.
