# PostgreSQL

**Why:** data is relational (users, productions, members, tasks, schedule)
with foreign keys and multi-table transactions. Postgres is boring,
well-understood, available everywhere.

**Not:**
- **MySQL** — fine, but Postgres has stronger semantics for what we'll
  reach for next (JSONB, partial indexes, `LISTEN`/`NOTIFY`).
- **SQLite server-side** — single-writer, breaks at instance #2.
- **Mongo / Dynamo** — joins would move into app code.
- **Firebase / Supabase** — vendor lock-in on auth + schema.
