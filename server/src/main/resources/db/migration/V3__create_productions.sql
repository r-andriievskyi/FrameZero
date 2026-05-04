CREATE TABLE IF NOT EXISTS productions (
    id             UUID         PRIMARY KEY,
    title          VARCHAR(120) NOT NULL,
    genre          VARCHAR(20)  NOT NULL,
    logline        VARCHAR(280),
    phase          VARCHAR(20)  NOT NULL,
    start_date     DATE         NOT NULL,
    wrap_date      DATE         NOT NULL,
    budget_cents   BIGINT,
    owner_user_id  UUID         NOT NULL REFERENCES users(id),
    deleted_at     TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_productions_owner ON productions(owner_user_id);
CREATE INDEX IF NOT EXISTS idx_productions_updated_at ON productions(updated_at DESC);
