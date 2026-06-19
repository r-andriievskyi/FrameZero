-- Per-device FCM registration tokens. A user can have several (phone, tablet);
-- the token is globally unique so re-registering the same device just moves the
-- row to the current user. Keep this in sync with DeviceTokensTable.

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
