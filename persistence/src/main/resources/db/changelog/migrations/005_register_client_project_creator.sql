--liquibase formatted sql

-- ============================================================================
-- changeset ods:005-register-client-app-project-creator
-- Description: Register a client application with project creation permissions.
--   Client ID: 56a0fc62-bf77-4acb-8cd7-8cc9f5f2198f
--   Permissions: create projects (POST /api/pub/v0/projects)
-- ============================================================================

-- Register or update the client application
INSERT INTO client_apps (id, client_id, client_name, enabled, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    '56a0fc62-bf77-4acb-8cd7-8cc9f5f2198f',
    'Project Creator Application',
    true,
    NOW(),
    NOW()
)
ON CONFLICT (client_id) DO UPDATE SET
    enabled = true,
    updated_at = NOW();

-- Get references to the client and the api-writer role
WITH client_ref AS (
    SELECT id FROM client_apps
    WHERE client_id = '56a0fc62-bf77-4acb-8cd7-8cc9f5f2198f'
),
role_ref AS (
    SELECT id FROM roles
    WHERE name = 'api-writer'
),
resource_ref AS (
    SELECT id FROM api_resources
    WHERE http_method = 'POST' AND pattern = '/api/pub/v0/projects'
)
-- Assign api-writer role to the client (if not already assigned)
INSERT INTO client_app_roles (id, client_app_id, role_id, created_at)
SELECT gen_random_uuid(), c.id, r.id, NOW()
FROM client_ref c, role_ref r
WHERE NOT EXISTS (
    SELECT 1 FROM client_app_roles
    WHERE client_app_id = c.id AND role_id = r.id
)
ON CONFLICT DO NOTHING;

-- Grant api-writer role access to the project creation endpoint (if not already granted)
WITH role_ref AS (
    SELECT id FROM roles
    WHERE name = 'api-writer'
),
resource_ref AS (
    SELECT id FROM api_resources
    WHERE http_method = 'POST' AND pattern = '/api/pub/v0/projects'
)
INSERT INTO role_api_permissions (id, role_id, api_resource_id, allowed, created_at)
SELECT gen_random_uuid(), r.id, ar.id, true, NOW()
FROM role_ref r, resource_ref ar
WHERE NOT EXISTS (
    SELECT 1 FROM role_api_permissions
    WHERE role_id = r.id AND api_resource_id = ar.id
)
ON CONFLICT DO NOTHING;

--rollback WITH client_ref AS (
--rollback     SELECT id FROM client_apps
--rollback     WHERE client_id = '56a0fc62-bf77-4acb-8cd7-8cc9f5f2198f'
--rollback ),
--rollback role_ref AS (
--rollback     SELECT id FROM roles
--rollback     WHERE name = 'api-writer'
--rollback )
--rollback DELETE FROM client_app_roles
--rollback WHERE client_app_id IN (SELECT id FROM client_ref)
--rollback   AND role_id IN (SELECT id FROM role_ref);
--rollback
--rollback DELETE FROM client_apps
--rollback WHERE client_id = '56a0fc62-bf77-4acb-8cd7-8cc9f5f2198f';
