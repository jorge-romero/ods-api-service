--liquibase formatted sql

-- ============================================================================
-- changeset ods:006-create-api-definitions
-- Description: API definitions table (replaces api_resources for the new
--   policy-engine model). Each row represents a managed API module.
-- ============================================================================
CREATE TABLE IF NOT EXISTS api_definitions (
    id          UUID            NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    api_id      VARCHAR(100)    NOT NULL,
    name        VARCHAR(255)    NOT NULL,
    base_path   VARCHAR(255)    NOT NULL,
    version     VARCHAR(20)     NOT NULL,
    auth_types  VARCHAR(30)[]   NOT NULL DEFAULT '{}',
    is_public   BOOLEAN         NOT NULL DEFAULT FALSE,
    proxy_url   VARCHAR(500),
    enabled     BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_api_definitions_api_id
    ON api_definitions (api_id);

COMMENT ON TABLE  api_definitions            IS 'Managed API modules with auth and routing metadata';
COMMENT ON COLUMN api_definitions.api_id     IS 'Unique logical identifier (e.g. project-v0, project-users-v1)';
COMMENT ON COLUMN api_definitions.base_path  IS 'URL base path (e.g. projects, projects/users)';
COMMENT ON COLUMN api_definitions.version    IS 'API version (e.g. v0, v1)';
COMMENT ON COLUMN api_definitions.auth_types IS 'Allowed OAuth2 flow types: NONE, OBO, CLIENT_CREDENTIALS';
COMMENT ON COLUMN api_definitions.is_public  IS 'When TRUE the API is accessible without authentication';
COMMENT ON COLUMN api_definitions.proxy_url  IS 'If set, requests are proxied to this URL (null = local controller)';

--rollback DROP INDEX IF EXISTS uq_api_definitions_api_id;
--rollback DROP TABLE IF EXISTS api_definitions;


-- ============================================================================
-- changeset ods:006-create-clients
-- Description: Registered OAuth2 clients (simplified from client_apps).
-- ============================================================================
CREATE TABLE IF NOT EXISTS clients (
    id                UUID            NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    azure_client_id   VARCHAR(36)     NOT NULL,
    name              VARCHAR(255)    NOT NULL,
    enabled           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_clients_azure_client_id
    ON clients (azure_client_id);

COMMENT ON TABLE  clients                   IS 'Registered OAuth2 / Azure AD application clients';
COMMENT ON COLUMN clients.azure_client_id   IS 'Azure AD Application (client) UUID';
COMMENT ON COLUMN clients.name              IS 'Human-readable application name';

--rollback DROP INDEX IF EXISTS uq_clients_azure_client_id;
--rollback DROP TABLE IF EXISTS clients;


-- ============================================================================
-- changeset ods:006-create-authorization-policies
-- Description: Flexible, JSON-configured authorization policies.
--   Each policy ties an API definition to optional client-specific rules.
--   policy_config stores evaluator-specific parameters as JSONB.
-- ============================================================================
CREATE TABLE IF NOT EXISTS authorization_policies (
    id                  UUID            NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    api_definition_id   VARCHAR(100)    NOT NULL,
    client_id           VARCHAR(36),
    policy_type         VARCHAR(100)    NOT NULL,
    policy_config       JSONB,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_auth_policies_api_def_id
    ON authorization_policies (api_definition_id);

CREATE INDEX IF NOT EXISTS idx_auth_policies_client_id
    ON authorization_policies (client_id);

COMMENT ON TABLE  authorization_policies                    IS 'JSON-configured authorization policy rules';
COMMENT ON COLUMN authorization_policies.api_definition_id  IS 'Logical API id (matches api_definitions.api_id)';
COMMENT ON COLUMN authorization_policies.client_id          IS 'Azure client id (NULL = global rule for all clients)';
COMMENT ON COLUMN authorization_policies.policy_type        IS 'Evaluator type (e.g. ALLOWED_CLIENTS, SCOPE_REQUIRED, FLAVOR_RESTRICTION)';
COMMENT ON COLUMN authorization_policies.policy_config      IS 'Evaluator-specific config as JSONB';

--rollback DROP INDEX IF EXISTS idx_auth_policies_client_id;
--rollback DROP INDEX IF EXISTS idx_auth_policies_api_def_id;
--rollback DROP TABLE IF EXISTS authorization_policies;


-- ============================================================================
-- changeset ods:006-migrate-clients-from-client-apps
-- Description: Copy existing client_apps data into the new clients table.
-- ============================================================================
INSERT INTO clients (azure_client_id, name, enabled, created_at, updated_at)
SELECT ca.client_id, COALESCE(ca.client_name, 'Unknown'), ca.enabled, ca.created_at, ca.updated_at
FROM client_apps ca
ON CONFLICT (azure_client_id) DO NOTHING;

--rollback DELETE FROM clients WHERE azure_client_id IN (SELECT client_id FROM client_apps);


-- ============================================================================
-- changeset ods:006-seed-api-definitions
-- Description: Seed API definitions from the existing api_resources entries.
-- ============================================================================
INSERT INTO api_definitions (api_id, name, base_path, version, auth_types, is_public, enabled)
VALUES
    ('project-v0',       'Project API v0',       'projects',       'v0', ARRAY['CLIENT_CREDENTIALS'], FALSE, TRUE),
    ('project-users-v1', 'Project Users API v1', 'projects/users', 'v1', ARRAY['CLIENT_CREDENTIALS', 'OBO'], FALSE, TRUE)
ON CONFLICT (api_id) DO NOTHING;

--rollback DELETE FROM api_definitions WHERE api_id IN ('project-v0', 'project-users-v1');


-- ============================================================================
-- changeset ods:006-seed-allowed-clients-policy
-- Description: Create an ALLOWED_CLIENTS policy for the project-v0 API
--   based on the existing client that had api-writer role with POST permission.
-- ============================================================================
INSERT INTO authorization_policies (api_definition_id, client_id, policy_type, policy_config)
SELECT
    'project-v0',
    ca.client_id,
    'ALLOWED_CLIENTS',
    jsonb_build_object('allowedClients', jsonb_build_array(ca.client_id))
FROM client_apps ca
WHERE ca.client_id = '56a0fc62-bf77-4acb-8cd7-8cc9f5f2198f'
  AND ca.enabled = true
ON CONFLICT DO NOTHING;

--rollback DELETE FROM authorization_policies
--rollback WHERE api_definition_id = 'project-v0'
--rollback   AND policy_type = 'ALLOWED_CLIENTS';
