CREATE TABLE IF NOT EXISTS tasks (
    id               UUID         PRIMARY KEY,
    production_id    UUID         NOT NULL REFERENCES productions(id),
    title            VARCHAR(200) NOT NULL,
    description      TEXT,
    due_date         DATE,
    status           VARCHAR(10)  NOT NULL DEFAULT 'OPEN',
    assignee_user_id UUID         REFERENCES users(id),
    created_at       TIMESTAMPTZ  NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_tasks_production ON tasks(production_id);
CREATE INDEX IF NOT EXISTS idx_tasks_assignee ON tasks(assignee_user_id);
CREATE INDEX IF NOT EXISTS idx_tasks_due_date ON tasks(due_date ASC NULLS LAST);
