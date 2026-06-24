-- Single optional file attachment per task, plus an idempotency key on tasks so a
-- retried background upload (WorkManager / iOS background URLSession) can't create a
-- duplicate task. The blob itself lives on the filesystem (FILE_STORAGE_DIR); only
-- metadata is stored here. Keep this in sync with TasksTable / TaskAttachmentsTable.

ALTER TABLE tasks
    ADD COLUMN idempotency_key VARCHAR(64) NULL;
CREATE UNIQUE INDEX tasks_idempotency_key_unique ON tasks (idempotency_key);

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
