BEGIN;

ALTER TABLE user_knowledge_base
    DROP COLUMN IF EXISTS embedding;

ALTER TABLE linkedin_jobs
    DROP COLUMN IF EXISTS embedding;

COMMIT;