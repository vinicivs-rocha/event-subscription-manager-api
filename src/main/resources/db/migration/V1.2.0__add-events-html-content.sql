ALTER TABLE events
    ADD advertising_content TEXT NOT NULL DEFAULT '';
ALTER TABLE events
    ALTER COLUMN title TYPE TEXT;