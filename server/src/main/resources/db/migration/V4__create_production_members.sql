CREATE TABLE IF NOT EXISTS production_members (
    id             UUID         PRIMARY KEY,
    production_id  UUID         NOT NULL REFERENCES productions(id),
    user_id        UUID         REFERENCES users(id),
    name           VARCHAR(200) NOT NULL,
    role           VARCHAR(100) NOT NULL,
    email          VARCHAR(320),
    added_at       TIMESTAMPTZ  NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_members_production ON production_members(production_id);
CREATE INDEX IF NOT EXISTS idx_members_user ON production_members(user_id);
