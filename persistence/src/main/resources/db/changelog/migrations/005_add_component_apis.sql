--liquibase formatted sql

-- ============================================================================
-- changeset ods:007-update-platform-api-visibility
-- Description: Set project-platforms-v1 API as public.
-- ============================================================================
UPDATE api_definitions
SET is_public = TRUE
WHERE api_id = 'project-platforms-v1';

--rollback UPDATE api_definitions SET is_public = FALSE WHERE api_id = 'project-platforms-v1';


-- ============================================================================
-- changeset ods:008-seed-component-api-definitions
-- Description: Seed API definitions for Project Components APIs v0 and v1.
-- ============================================================================
INSERT INTO api_definitions (api_id, name, base_path, version, auth_types, is_public, enabled)
VALUES
    ('project-component-v1', 'Project Components API v1', 'projects/*/components', 'v1', ARRAY['CLIENT_CREDENTIALS', 'OBO'], FALSE, TRUE),
    ('project-component-v0', 'Project Components API v0', 'projects/*/components', 'v0', ARRAY['CLIENT_CREDENTIALS', 'OBO'], FALSE, TRUE)
ON CONFLICT (api_id) DO NOTHING;

--rollback DELETE FROM api_definitions WHERE api_id IN ('project-component-v1', 'project-component-v0');


