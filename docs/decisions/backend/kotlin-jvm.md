# Kotlin on the JVM

**Why:** the clients are Kotlin Multiplatform. Same language on the server
means the wire DTOs and enums are plain Kotlin on both ends — copy-paste
compatible, so the server keeps its own duplicated copy (see
[duplicated-dtos](duplicated-dtos.md)) with no codegen or cross-language
mapping, and stays liftable into a standalone repo.

**Not:**
- **Node/TS, Go** — a *cross-language* boundary: the shared shapes can't be
  copied verbatim, forcing codegen or a spec to stay in sync.
- **Kotlin/Native** — immature HTTP/JDBC story, no payoff for a
  non-latency-critical service.

**Cost:** JVM startup + memory. Irrelevant for one long-running service.
