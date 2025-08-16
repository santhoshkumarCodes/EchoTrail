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
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content_markdown TEXT,
    content_html TEXT,
    is_public BOOLEAN DEFAULT false,
    is_unlocked BOOLEAN DEFAULT false,
    is_chained BOOLEAN DEFAULT false,
    unlock_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_capsule_user_id ON capsule(user_id);
CREATE INDEX idx_capsule_is_public ON capsule(is_public);
CREATE INDEX idx_capsule_unlock_at ON capsule(unlock_at);

CREATE TABLE IF NOT EXISTS capsule_chain (
    id BIGSERIAL PRIMARY KEY,
    capsule_id BIGINT NOT NULL,
    previous_capsule_id BIGINT,
    next_capsule_id BIGINT,
    FOREIGN KEY (capsule_id) REFERENCES capsule(id) ON DELETE CASCADE,
    FOREIGN KEY (previous_capsule_id) REFERENCES capsule(id) ON DELETE SET NULL,
    FOREIGN KEY (next_capsule_id) REFERENCES capsule(id) ON DELETE SET NULL
);

CREATE INDEX idx_capsule_chain_capsule_id ON capsule_chain(capsule_id);
CREATE INDEX idx_capsule_chain_previous_capsule_id ON capsule_chain(previous_capsule_id);
CREATE INDEX idx_capsule_chain_next_capsule_id ON capsule_chain(next_capsule_id);

CREATE TABLE IF NOT EXISTS capsule_visibility (
    id BIGSERIAL PRIMARY KEY,
    capsule_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (capsule_id) REFERENCES capsule(id) ON DELETE CASCADE,
    UNIQUE (capsule_id, user_id)
);

CREATE INDEX idx_capsule_visibility_capsule_id ON capsule_visibility(capsule_id);
CREATE INDEX idx_capsule_visibility_user_id ON capsule_visibility(user_id);

\c "Friendship"

CREATE TABLE IF NOT EXISTS friendships (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE (sender_id, receiver_id)
);

CREATE INDEX idx_friendships_sender_id ON friendships(sender_id);
CREATE INDEX idx_friendships_receiver_id ON friendships(receiver_id);
CREATE INDEX idx_friendships_status ON friendships(status);

\c "User"

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    auth_provider VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
