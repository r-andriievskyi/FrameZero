ALTER TABLE users
    ADD COLUMN IF NOT EXISTS avatar_color_hex VARCHAR(7);
