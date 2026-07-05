-- Squashed baseline schema (folds former V1..V6). Generated to match the Exposed
-- Table definitions in server/src/main. Flyway owns DDL from here on; the Exposed
-- tables remain the typed query surface. Keep this file and the Table definitions
-- in sync: add a new V<n>__*.sql migration for any schema change rather than
-- editing this one.

CREATE TABLE users (
    id               UUID PRIMARY KEY,
    email            VARCHAR(320) NOT NULL,
    password_hash    VARCHAR(100) NOT NULL,
    first_name       VARCHAR(100) NOT NULL,
    last_name        VARCHAR(100) NOT NULL,
    avatar_color_hex VARCHAR(7)   NULL,
    created_at       TIMESTAMP    NOT NULL
);
CREATE UNIQUE INDEX users_email_unique ON users (email);

CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY,
    user_id     UUID         NOT NULL CONSTRAINT fk_refresh_tokens_user_id__id REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    token_hash  VARCHAR(64)  NOT NULL,
    expires_at  TIMESTAMP    NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL
);
CREATE UNIQUE INDEX refresh_tokens_token_hash_unique ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);

CREATE TABLE productions (
    id            UUID PRIMARY KEY,
    title         VARCHAR(120) NOT NULL,
    genre         VARCHAR(20)  NOT NULL,
    logline       VARCHAR(280) NULL,
    phase         VARCHAR(20)  NOT NULL,
    start_date    DATE         NOT NULL,
    wrap_date     DATE         NOT NULL,
    budget_cents  BIGINT       NULL,
    owner_user_id UUID         NOT NULL CONSTRAINT fk_productions_owner_user_id__id REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    deleted_at    TIMESTAMP    NULL,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL
);
CREATE INDEX idx_productions_owner ON productions (owner_user_id);
CREATE INDEX idx_productions_updated_at ON productions (updated_at);

CREATE TABLE production_members (
    id                   UUID PRIMARY KEY,
    production_id        UUID         NOT NULL CONSTRAINT fk_production_members_production_id__id REFERENCES productions (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    user_id              UUID         NULL CONSTRAINT fk_production_members_user_id__id REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    name                 VARCHAR(200) NOT NULL,
    role                 VARCHAR(100) NOT NULL,
    email                VARCHAR(320) NULL,
    added_at             TIMESTAMP    NOT NULL,
    reports_to_member_id UUID         NULL
);
CREATE INDEX idx_members_production ON production_members (production_id);
CREATE INDEX idx_members_user ON production_members (user_id);
CREATE INDEX idx_members_reports_to ON production_members (reports_to_member_id);

CREATE TABLE tasks (
    id                 UUID PRIMARY KEY,
    production_id      UUID         NOT NULL CONSTRAINT fk_tasks_production_id__id REFERENCES productions (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    title              VARCHAR(200) NOT NULL,
    description        TEXT         NULL,
    due_date           DATE         NULL,
    status             VARCHAR(10)  NOT NULL,
    priority           VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM',
    assignee_user_id   UUID         NULL CONSTRAINT fk_tasks_assignee_user_id__id REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    idempotency_key    VARCHAR(64)  NULL,
    created_by_user_id UUID         NULL CONSTRAINT fk_tasks_created_by_user_id__id REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    created_at         TIMESTAMP    NOT NULL
);
CREATE INDEX idx_tasks_production ON tasks (production_id);
CREATE INDEX idx_tasks_assignee ON tasks (assignee_user_id);
CREATE INDEX idx_tasks_due_date ON tasks (due_date);
CREATE UNIQUE INDEX tasks_idempotency_key_unique ON tasks (idempotency_key);
CREATE INDEX idx_tasks_created_by ON tasks (created_by_user_id);

CREATE TABLE task_attachments (
    id           UUID PRIMARY KEY,
    task_id      UUID         NOT NULL CONSTRAINT fk_task_attachments_task_id__id REFERENCES tasks (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    file_name    VARCHAR(255) NOT NULL,
    content_type VARCHAR(127) NOT NULL,
    size_bytes   BIGINT       NOT NULL,
    storage_key  VARCHAR(255) NOT NULL,
    created_at   TIMESTAMP    NOT NULL
);
CREATE UNIQUE INDEX task_attachments_task_unique ON task_attachments (task_id);

CREATE TABLE task_participants (
    task_id UUID NOT NULL CONSTRAINT fk_task_participants_task_id__id REFERENCES tasks (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    user_id UUID NOT NULL CONSTRAINT fk_task_participants_user_id__id REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    PRIMARY KEY (task_id, user_id)
);
CREATE INDEX idx_task_participants_user ON task_participants (user_id);

CREATE TABLE schedule_events (
    id            UUID PRIMARY KEY,
    production_id UUID         NOT NULL CONSTRAINT fk_schedule_events_production_id__id REFERENCES productions (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    title         VARCHAR(200) NOT NULL,
    location      VARCHAR(300) NULL,
    starts_at     TIMESTAMP    NOT NULL,
    ends_at       TIMESTAMP    NOT NULL,
    kind          VARCHAR(20)  NOT NULL
);
CREATE INDEX idx_events_production ON schedule_events (production_id);
CREATE INDEX idx_events_starts_at ON schedule_events (starts_at);

CREATE TABLE notifications (
    id         UUID PRIMARY KEY,
    user_id    UUID         NOT NULL CONSTRAINT fk_notifications_user_id__id REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    body       TEXT         NULL,
    read_at    TIMESTAMP    NULL,
    created_at TIMESTAMP    NOT NULL
);
CREATE INDEX idx_notifications_user ON notifications (user_id, created_at);

CREATE TABLE device_tokens (
    id         UUID PRIMARY KEY,
    user_id    UUID         NOT NULL CONSTRAINT fk_device_tokens_user_id__id REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    token      VARCHAR(512) NOT NULL,
    platform   VARCHAR(10)  NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL
);
CREATE UNIQUE INDEX device_tokens_token_unique ON device_tokens (token);
CREATE INDEX idx_device_tokens_user ON device_tokens (user_id);

-- Task-scoped group chats over a generic conversation model. conversations(kind)
-- is generic from day one so DMs become "a new kind" later, not a schema rework.
CREATE TABLE conversations (
    id            UUID PRIMARY KEY,
    kind          VARCHAR(20) NOT NULL,
    task_id       UUID        NULL CONSTRAINT fk_conversations_task_id__id REFERENCES tasks (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    production_id UUID        NOT NULL CONSTRAINT fk_conversations_production_id__id REFERENCES productions (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    created_at    TIMESTAMP   NOT NULL
);
CREATE UNIQUE INDEX conversations_task_unique ON conversations (task_id);
CREATE INDEX idx_conversations_production ON conversations (production_id);

CREATE TABLE conversation_participants (
    conversation_id   UUID      NOT NULL CONSTRAINT fk_conversation_participants_conversation_id__id REFERENCES conversations (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    user_id           UUID      NOT NULL CONSTRAINT fk_conversation_participants_user_id__id REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    last_read_ordinal BIGINT    NOT NULL,
    joined_at         TIMESTAMP NOT NULL,
    PRIMARY KEY (conversation_id, user_id)
);
CREATE INDEX idx_conversation_participants_user ON conversation_participants (user_id);

CREATE TABLE messages (
    id                UUID        PRIMARY KEY,
    conversation_id   UUID        NOT NULL CONSTRAINT fk_messages_conversation_id__id REFERENCES conversations (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    ordinal           BIGINT      NOT NULL,
    sender_user_id    UUID        NOT NULL CONSTRAINT fk_messages_sender_user_id__id REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    body              TEXT        NOT NULL,
    client_message_id VARCHAR(64) NOT NULL,
    created_at        TIMESTAMP   NOT NULL
);
CREATE UNIQUE INDEX messages_conversation_ordinal_unique ON messages (conversation_id, ordinal);
CREATE UNIQUE INDEX messages_conv_sender_client_id_unique ON messages (conversation_id, sender_user_id, client_message_id);
