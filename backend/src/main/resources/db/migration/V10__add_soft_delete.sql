-- V10__add_soft_delete.sql
-- Add soft delete support to all resource tables

-- Add deleted_at column to user table
ALTER TABLE app_users ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE NULL;
CREATE INDEX idx_app_users_deleted_at ON app_users(deleted_at);

-- Add deleted_at column to health_profile table
ALTER TABLE health_profile ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE NULL;
CREATE INDEX idx_health_profile_deleted_at ON health_profile(deleted_at);

-- Add deleted_at column to goal table
ALTER TABLE goal ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE NULL;
CREATE INDEX idx_goal_deleted_at ON goal(deleted_at);

-- Add deleted_at column to weight_entry table
ALTER TABLE weight_entry ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE NULL;
CREATE INDEX idx_weight_entry_deleted_at ON weight_entry(deleted_at);

-- Add deleted_at column to goal_progress table
ALTER TABLE goal_progress ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE NULL;
CREATE INDEX idx_goal_progress_deleted_at ON goal_progress(deleted_at);

-- Add deleted_at column to ai_insight table
ALTER TABLE ai_insight ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE NULL;
CREATE INDEX idx_ai_insight_deleted_at ON ai_insight(deleted_at);

-- Note: audit_events is not soft-deleted (immutable audit trail)
-- Note: email_verification_codes and password_reset_tokens are not soft-deleted (temporary tokens)
