# Shared kotlinx.serialization DTOs

**Why:** DTOs defined once in `shared/commonMain/dto/`, consumed unchanged
by Ktor server and Ktor client. Renaming a server field is a compile error
on every client. Wire format carries semantic fields only — no UI hints
(colors, labels) over the wire; clients derive those in mappers.

**Not:**
- **OpenAPI + codegen** — extra spec artefact + generator quirks; both
  ends are Kotlin, so why.
- **gRPC / Protobuf** — loses browser-friendly JSON, debugging with
  curl, and symmetry with the rest of the stack.
- **Hand-typed DTOs on each side** — guaranteed drift.

**Cost:** discipline — nothing UI-shaped in `dto/`. Documented in
`CLAUDE.md`.
