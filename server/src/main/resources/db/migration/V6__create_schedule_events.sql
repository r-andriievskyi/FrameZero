CREATE TABLE IF NOT EXISTS schedule_events (
    id             UUID         PRIMARY KEY,
    production_id  UUID         NOT NULL REFERENCES productions(id),
    title          VARCHAR(200) NOT NULL,
    location       VARCHAR(300),
    starts_at      TIMESTAMPTZ  NOT NULL,
    ends_at        TIMESTAMPTZ  NOT NULL,
    kind           VARCHAR(20)  NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_events_production ON schedule_events(production_id);
CREATE INDEX IF NOT EXISTS idx_events_starts_at ON schedule_events(starts_at);
