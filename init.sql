-- The "Capsules" database is created automatically by the postgres container
-- because POSTGRES_DB is set to "Capsules" in the .env file.
-- This script is executed within the "Capsules" database context.

CREATE DATABASE "Friendship";
CREATE DATABASE "User";

-- Now, create the outbox_event table in the "Capsules" database (which is the current one)
CREATE TABLE outbox_event (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE capsule (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    title VARCHAR(255),
    content_markdown TEXT,
    content_html TEXT,
    is_public BOOLEAN DEFAULT false,
    is_unlocked BOOLEAN DEFAULT false,
    is_chained BOOLEAN,
    unlock_at TIMESTAMP,
    created_at TIMESTAMP
);

CREATE TABLE capsule_chain (
    id BIGSERIAL PRIMARY KEY,
    capsule_id BIGINT,
    previous_capsule_id BIGINT,
    next_capsule_id BIGINT
);

CREATE TABLE capsule_visibility (
    id BIGSERIAL PRIMARY KEY,
    capsule_id BIGINT,
    user_id BIGINT
);

-- Switch to the other databases to create the same table.
\c "Friendship"

CREATE TABLE outbox_event (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

\c "User"

CREATE TABLE outbox_event (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
