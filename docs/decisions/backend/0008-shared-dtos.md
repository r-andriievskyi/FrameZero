# 0008 — Shared kotlinx.serialization DTOs as the wire contract

- **Status:** Accepted
- **Date:** 2026-05-11

## Context

The server speaks JSON to four clients (Android, iOS, Desktop, Web) that all
share `commonMain` Kotlin code. The largest class of shipping bug in a
client/server product is wire-format drift: a field renamed on one side, an
enum value added on the other, a nullable that wasn't.

We have a unique advantage: client and server are both Kotlin, both can
depend on the same Gradle module, and both use the same serialization
library.

## Decision

DTOs are defined **once**, in `shared/src/commonMain/kotlin/com/frame/zero/dto/`,
organised by feature. They are annotated `@Serializable` and consumed
unchanged by both `composeApp` (via Ktor client) and `server` (via Ktor
server `ContentNegotiation { json() }`). Constants like `SERVER_PORT` live in
`shared/Constants.kt`.

The wire format carries **only semantic fields**: enums, ids, status, phase,
timestamps. UI hints (accent colors, badge labels, formatted strings) are
derived client-side in mappers — never sent over the wire.

## Alternatives

- **OpenAPI / spec-first with codegen** — language-agnostic and tool-rich,
  but introduces a generation step, a parallel artefact (`openapi.yaml`) to
  keep in sync with the code, and a generator's idiosyncrasies in the
  generated types. Overkill when both ends are Kotlin.
- **Protobuf / gRPC** — strong contract and good codegen, but JSON over
  HTTP is friendlier for browser clients, debugging with `curl`, and the
  caching/CDN story. We'd also lose `kotlinx.serialization`'s symmetry with
  the rest of the stack.
- **Hand-typed DTOs duplicated on each side** — guarantees drift. Rejected.

## Consequences

- Renaming a field on the server is a compile error on every client; the
  type system enforces the contract.
- A field added to a DTO is automatically available everywhere, but we still
  need to think about *forward compatibility*: older clients deserialising
  a payload with a new field. `kotlinx.serialization` ignores unknown
  fields by default, which is what we want.
- Discipline required: nothing UI-shaped goes in the DTO. Documented as a
  rule in `CLAUDE.md`. ADR violations show up as `*Color`, `*Hint`, or
  `displayLabel` fields in `dto/`.
- The REST contract is documented narratively in `API.md` at the repo root.
  The DTOs are the source of truth; the doc explains intent and routes.
