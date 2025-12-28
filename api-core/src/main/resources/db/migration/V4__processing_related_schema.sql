BEGIN;

-- add processed_user_data
ALTER TABLE user_info
ADD COLUMN processed_user_data jsonb,
ADD COLUMN processing_version varchar(10) DEFAULT 'v1',
ADD COLUMN processed_at timestamp without time zone;

-- add processed job data
ALTER TABLE linkedin_jobs
ADD COLUMN processed_job_data jsonb,
ADD COLUMN processing_version varchar(10) DEFAULT 'v1',
ADD COLUMN processed_at timestamp without time zone;

CREATE TABLE user_job_comparisons (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL,
    job_id varchar(10) NOT NULL,
    comparison_data jsonb NOT NULL,
    match_score decimal(5,2),
    processing_version varchar(10) DEFAULT 'v1',
    created_at timestamp without time zone DEFAULT now(),
    updated_at timestamp without time zone DEFAULT now(),

    CONSTRAINT user_job_comparisons_pkey PRIMARY KEY (id),
    CONSTRAINT user_job_comparisons_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT user_job_comparisons_job_id_fkey FOREIGN KEY (job_id) REFERENCES linkedin_jobs(job_id) ON DELETE CASCADE,
    CONSTRAINT uq_user_job_version UNIQUE (user_id, job_id, processing_version)
);


-- add trigger for updated_at
CREATE TRIGGER update_user_job_comparisons_updated_at
    BEFORE UPDATE ON user_job_comparisons
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMIT;