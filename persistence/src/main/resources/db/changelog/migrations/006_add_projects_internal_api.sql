--liquibase formatted sql

-- ============================================================================
-- changeset ods:009-add-project-v1-api-definition
-- Description: Seed API definitions for Project API v1.
-- ============================================================================
INSERT INTO api_definitions (api_id, name, base_path, version, auth_types, is_public, enabled)
VALUES
    ('project-v1', 'Project API v1', 'projects', 'v1', ARRAY['CLIENT_CREDENTIALS'], FALSE, TRUE)
    ON CONFLICT (api_id) DO NOTHING;

--rollback DELETE FROM api_definitions WHERE api_id IN ('project-v1');