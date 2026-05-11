# Kotlin on the JVM

**Why:** the clients are Kotlin Multiplatform. Same language on the server
means DTOs, enums, and constants live in `shared` and are imported by both
sides — the wire contract cannot drift silently.

**Not:**
- **Node/TS, Go** — force DTO duplication, lose the typed contract.
- **Kotlin/Native** — immature HTTP/JDBC story, no payoff for a
  non-latency-critical service.

**Cost:** JVM startup + memory. Irrelevant for one long-running service.
