-- Managed by core-api service
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR NOT NULL UNIQUE,
    github_username VARCHAR NOT NULL UNIQUE,
    github_token VARCHAR NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
    );

-- "State" of the user across all the platform
CREATE TABLE IF NOT EXISTS user_info (
    user_id UUID PRIMARY KEY REFERENCES users(id),
    leetcode_stats JSONB,
    github_stats JSONB,
    resume_summary JSONB,
    last_ingested_at TIMESTAMP
    );

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS user_knowledge_base (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    content TEXT,
    source_type VARCHAR,
    source_url VARCHAR,
    embedding vector(1536) -- will try to make this dynamic in future for multi model support
);