# Duplicated wire DTOs (not a shared module)

**Why:** the server owns its **own copy** of the wire DTOs and enums
(`com.frame.zero.dto.*`, `auth.dto.*`, `Genre`/`ProductionPhase`/…) under
`server/src/main`, under the **same package names** as the client copy in
`shared/commonMain`. Same FQNs compiled from separate source = no build
dependency either way, so the server depends on **no client module** and can be
lifted into a standalone repo unchanged. Wire format carries semantic fields
only — no UI hints (colors, labels) over the wire; clients derive those in
mappers.

**Not:**
- **One shared `:dto` module imported by both** — couples the server to the
  client build and blocks extracting it to its own repo. The thing we
  explicitly gave up.
- **OpenAPI + codegen** — extra spec artefact + generator quirks; both ends are
  Kotlin already.
- **gRPC / Protobuf** — loses browser-friendly JSON, `curl` debugging, and
  symmetry with the rest of the stack.

**Cost:** the contract is **kept in sync by hand** — change a wire shape and you
edit both copies in the same change, or client and server drift silently.
Documented in `CLAUDE.md` ("Wire contract duplication").
