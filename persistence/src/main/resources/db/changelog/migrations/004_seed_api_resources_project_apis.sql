--liquibase formatted sql

-- ============================================================================
-- changeset ods:004-seed-api-resources-project-apis
-- Description: Seed API resources for project-related APIs.
-- ============================================================================
INSERT INTO api_resources (api_name, http_method, pattern, description)
VALUES
    ('project', 'POST', '/api/pub/v0/projects', 'Create project'),
    ('project', 'GET', '/api/pub/v0/projects/*', 'Get project by project key'),
    ('project-users', 'POST', '/api/v1/projects/*/users', 'Trigger membership request'),
    ('project-users', 'GET', '/api/v1/projects/*/users/*/status', 'Get membership request status')
ON CONFLICT DO NOTHING;

--rollback DELETE FROM api_resources
--rollback WHERE (http_method, pattern) IN (
--rollback     ('POST', '/api/pub/v0/projects'),
--rollback     ('GET', '/api/pub/v0/projects/*'),
--rollback     ('POST', '/api/v1/projects/*/users'),
--rollback     ('GET', '/api/v1/projects/*/users/*/status'),
--rollback );
