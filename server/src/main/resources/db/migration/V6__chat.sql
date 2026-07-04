-- Phase 1 of the chat feature: task-scoped group chats over a generic conversation
-- model. conversations(kind) is generic from day one so DMs become "a new kind"
-- later, not a schema rework. The MVP only creates TASK-kind conversations (one
-- per task, get-or-create). Keep this in sync with
-- ConversationsTable / ConversationParticipantsTable / MessagesTable.

CREATE TABLE conversations (
    id            UUID PRIMARY KEY,
    kind          VARCHAR(20) NOT NULL,
    -- Set for TASK-kind conversations; the unique index below maps one task to at
    -- most one conversation so get-or-create is race-safe. Null for kinds that
    -- aren't task-scoped (future DMs). CASCADE so deleting a task
    -- (task/TaskRepository.delete) takes its conversation and messages with it,
    -- matching task_attachments' delete-with-parent behavior (V4).
    task_id       UUID        NULL CONSTRAINT fk_conversations_task_id__id REFERENCES tasks (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    -- Denormalized so the defense-in-depth production-membership check doesn't need
    -- a tasks join on every authorization.
    production_id UUID        NOT NULL CONSTRAINT fk_conversations_production_id__id REFERENCES productions (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    created_at    TIMESTAMP   NOT NULL
);
CREATE UNIQUE INDEX conversations_task_unique ON conversations (task_id);
CREATE INDEX idx_conversations_production ON conversations (production_id);

CREATE TABLE conversation_participants (
    -- CASCADE: a conversation participant row is worthless once its conversation
    -- is gone (e.g. cascaded from the owning task's deletion above).
    conversation_id UUID      NOT NULL CONSTRAINT fk_conversation_participants_conversation_id__id REFERENCES conversations (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    user_id         UUID      NOT NULL CONSTRAINT fk_conversation_participants_user_id__id REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    -- Read-state, not ACL: rows are created lazily on first interaction (subscribe,
    -- send). Authorization is always evaluated against the task circle at request
    -- time, so a stale row here never grants access.
    last_read_ordinal   BIGINT    NOT NULL,
    joined_at       TIMESTAMP NOT NULL,
    PRIMARY KEY (conversation_id, user_id)
);
CREATE INDEX idx_conversation_participants_user ON conversation_participants (user_id);

CREATE TABLE messages (
    id                UUID        PRIMARY KEY,
    -- CASCADE: message history dies with its conversation (which itself cascades
    -- from the owning task's deletion).
    conversation_id   UUID        NOT NULL CONSTRAINT fk_messages_conversation_id__id REFERENCES conversations (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    -- Server-assigned monotonic counter per conversation (not a timestamp): gap
    -- detection on reconnect is "give me everything after ordinal N", and read markers
    -- become exact. The unique index makes the counter's contract enforceable.
    ordinal               BIGINT      NOT NULL,
    sender_user_id    UUID        NOT NULL CONSTRAINT fk_messages_sender_user_id__id REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    body              TEXT        NOT NULL,
    -- Client-generated idempotency token: a retried send with the same value from
    -- the same sender in the same conversation collapses to one row. Scoped to the
    -- conversation (not sender-global) so a client that reuses ids across
    -- conversations can't collide two unrelated messages.
    client_message_id VARCHAR(64) NOT NULL,
    created_at        TIMESTAMP   NOT NULL
);
CREATE UNIQUE INDEX messages_conversation_ordinal_unique ON messages (conversation_id, ordinal);
CREATE UNIQUE INDEX messages_conv_sender_client_id_unique ON messages (conversation_id, sender_user_id, client_message_id);
