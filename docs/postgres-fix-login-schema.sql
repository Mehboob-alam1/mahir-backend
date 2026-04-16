-- Run once on an existing DigitalOcean / Postgres DB if login fails with
-- "relation ... does not exist" (often misread as "order does not exist")
-- or "column ... does not exist" after upgrading the API.
-- Connect: psql "postgresql://doadmin:PASSWORD@HOST:25060/defaultdb?sslmode=require"
-- Or use DO SQL console → Query.

-- Mahir ↔ category join (lazy-loaded after sign-in when building user JSON)
CREATE TABLE IF NOT EXISTS user_service_categories (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, category_id)
);

CREATE INDEX IF NOT EXISTS idx_user_service_categories_user_id ON user_service_categories(user_id);

-- users.* columns expected by current JPA User entity (safe if already present)
ALTER TABLE users ADD COLUMN IF NOT EXISTS account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE users ADD COLUMN IF NOT EXISTS blocked BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS blocked_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS blocked_reason VARCHAR(500);
