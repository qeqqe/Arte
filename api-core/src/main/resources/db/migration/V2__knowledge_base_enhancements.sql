-- add metadata and tracking columns to user_knowledge_base
ALTER TABLE user_knowledge_base 
    ADD COLUMN IF NOT EXISTS metadata JSONB DEFAULT '{}',
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();

-- prevent duplicate entries per user/source
ALTER TABLE user_knowledge_base 
    ADD CONSTRAINT uq_user_source UNIQUE (user_id, source_type, source_url);

-- index on source_type for faster filtering
CREATE INDEX IF NOT EXISTS idx_knowledge_base_source_type 
    ON user_knowledge_base(source_type);

-- index on user_id for faster user lookups
CREATE INDEX IF NOT EXISTS idx_knowledge_base_user_id 
    ON user_knowledge_base(user_id);

-- updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- trigger to user_knowledge_base
DROP TRIGGER IF EXISTS update_user_knowledge_base_updated_at ON user_knowledge_base;
CREATE TRIGGER update_user_knowledge_base_updated_at
    BEFORE UPDATE ON user_knowledge_base
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- leetcode_username to users table for lc integration
ALTER TABLE users 
    ADD COLUMN IF NOT EXISTS leetcode_username VARCHAR;
