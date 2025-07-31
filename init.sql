CREATE DATABASE "Capsules";
CREATE DATABASE "Friendship";
CREATE DATABASE "User";

\c "Capsules"

CREATE TABLE IF NOT EXISTS outbox_event (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS capsule (
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

CREATE TABLE IF NOT EXISTS capsule_chain (
    id BIGSERIAL PRIMARY KEY,
    capsule_id BIGINT,
    previous_capsule_id BIGINT,
    next_capsule_id BIGINT
);

CREATE TABLE IF NOT EXISTS capsule_visibility (
    id BIGSERIAL PRIMARY KEY,
    capsule_id BIGINT,
    user_id BIGINT
);

\c "Friendship"

CREATE TABLE IF NOT EXISTS outbox_event (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

\c "User"

CREATE TABLE IF NOT EXISTS outbox_event (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
