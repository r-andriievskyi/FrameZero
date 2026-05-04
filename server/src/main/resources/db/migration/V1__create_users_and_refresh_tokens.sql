CREATE TABLE IF NOT EXISTS users (
    id               UUID        PRIMARY KEY,
    email            VARCHAR(320) NOT NULL UNIQUE,
    password_hash    VARCHAR(100) NOT NULL,
    first_name       VARCHAR(100) NOT NULL DEFAULT '',
    last_name        VARCHAR(100) NOT NULL DEFAULT '',
    created_at       TIMESTAMPTZ  NOT NULL
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id           UUID        PRIMARY KEY,
    user_id      UUID        NOT NULL REFERENCES users(id),
    token_hash   VARCHAR(64) NOT NULL UNIQUE,
    expires_at   TIMESTAMPTZ  NOT NULL,
    revoked      BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL
);
