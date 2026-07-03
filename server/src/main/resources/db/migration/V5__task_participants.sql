-- Phase 0 of the chat feature: tasks gain a many-to-many participants list while
-- assignee_user_id stays the single responsible person. Also records who created
-- the task (nullable for pre-existing rows) because only the creator or the
-- current assignee may modify the participant list. Keep this in sync with
-- TasksTable / TaskParticipantsTable.

ALTER TABLE tasks
    ADD COLUMN created_by_user_id UUID NULL CONSTRAINT fk_tasks_created_by_user_id__id REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT;
CREATE INDEX idx_tasks_created_by ON tasks (created_by_user_id);

CREATE TABLE task_participants (
    task_id UUID NOT NULL CONSTRAINT fk_task_participants_task_id__id REFERENCES tasks (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    user_id UUID NOT NULL CONSTRAINT fk_task_participants_user_id__id REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    PRIMARY KEY (task_id, user_id)
);
CREATE INDEX idx_task_participants_user ON task_participants (user_id);
