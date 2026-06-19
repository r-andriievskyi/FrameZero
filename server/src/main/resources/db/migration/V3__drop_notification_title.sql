-- Notification display titles are now derived client-side from the notification's
-- semantic context, so the server no longer stores a title. Body (the user-facing
-- content, e.g. the task title) stays.
ALTER TABLE notifications DROP COLUMN title;
