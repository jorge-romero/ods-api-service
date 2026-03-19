--liquibase formatted sql

-- ============================================================================
-- changeset ods:003-create-api-resources
-- Description: Catalogue of API endpoints that can be protected.
--   Each row represents a logical API operation (HTTP method + URL pattern).
-- ============================================================================
CREATE TABLE IF NOT EXISTS api_resources (
    id          UUID            NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    api_name    VARCHAR(100)    NOT NULL,
    http_method VARCHAR(10)     NOT NULL,
    pattern     VARCHAR(255)    NOT NULL,
    description VARCHAR(500),
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_api_resources_method_pattern
    ON api_resources (http_method, pattern);

COMMENT ON TABLE  api_resources               IS 'Catalogue of protectable API endpoints';
COMMENT ON COLUMN api_resources.api_name      IS 'Logical API group (e.g. project, project-users, project-platform)';
COMMENT ON COLUMN api_resources.http_method   IS 'HTTP method: GET, POST, PUT, DELETE, PATCH, or * for all';
COMMENT ON COLUMN api_resources.pattern       IS 'Ant-style URL pattern (e.g. /api/v1/projects/**)';
COMMENT ON COLUMN api_resources.description   IS 'Human-readable description of the endpoint';

--rollback DROP INDEX IF EXISTS uq_api_resources_method_pattern;
--rollback DROP TABLE IF EXISTS api_resources;


-- ============================================================================
-- changeset ods:003-create-roles
-- Description: Logical roles that can be assigned to client applications.
-- ============================================================================
CREATE TABLE IF NOT EXISTS roles (
    id          UUID            NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name        VARCHAR(100)    NOT NULL,
    description VARCHAR(500),
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_roles_name
    ON roles (name);

COMMENT ON TABLE  roles               IS 'Logical roles assignable to client applications';
COMMENT ON COLUMN roles.name          IS 'Role name (e.g. api-reader, api-writer, admin)';
COMMENT ON COLUMN roles.description   IS 'Human-readable description of the role';

--rollback DROP INDEX IF EXISTS uq_roles_name;
--rollback DROP TABLE IF EXISTS roles;


-- ============================================================================
-- changeset ods:003-create-client-app-roles
-- Description: Many-to-many link between client_apps and roles.
-- ============================================================================
CREATE TABLE IF NOT EXISTS client_app_roles (
    id             UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    client_app_id  UUID        NOT NULL,
    role_id        UUID        NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_client_app_roles_client_app
        FOREIGN KEY (client_app_id) REFERENCES client_apps (id) ON DELETE CASCADE,
    CONSTRAINT fk_client_app_roles_role
        FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_client_app_roles
    ON client_app_roles (client_app_id, role_id);

COMMENT ON TABLE  client_app_roles                IS 'Roles assigned to each client application';
COMMENT ON COLUMN client_app_roles.client_app_id  IS 'FK to client_apps.id';
COMMENT ON COLUMN client_app_roles.role_id        IS 'FK to roles.id';

--rollback DROP INDEX IF EXISTS uq_client_app_roles;
--rollback DROP TABLE IF EXISTS client_app_roles;


-- ============================================================================
-- changeset ods:003-create-role-api-permissions
-- Description: Grants a role access to a specific API resource.
--   The 'allowed' flag supports explicit deny overrides.
-- ============================================================================
CREATE TABLE IF NOT EXISTS role_api_permissions (
    id              UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    role_id         UUID        NOT NULL,
    api_resource_id UUID        NOT NULL,
    allowed         BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_role_api_permissions_role
        FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_api_permissions_api_resource
        FOREIGN KEY (api_resource_id) REFERENCES api_resources (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_role_api_permissions
    ON role_api_permissions (role_id, api_resource_id);

COMMENT ON TABLE  role_api_permissions                  IS 'Grants (or denies) a role access to an API resource';
COMMENT ON COLUMN role_api_permissions.role_id          IS 'FK to roles.id';
COMMENT ON COLUMN role_api_permissions.api_resource_id  IS 'FK to api_resources.id';
COMMENT ON COLUMN role_api_permissions.allowed          IS 'TRUE = allow, FALSE = explicit deny (deny wins)';

--rollback DROP INDEX IF EXISTS uq_role_api_permissions;
--rollback DROP TABLE IF EXISTS role_api_permissions;


-- ============================================================================
-- changeset ods:003-seed-default-roles
-- Description: Insert baseline roles matching common Entra ID App Roles.
-- ============================================================================
INSERT INTO roles (name, description) VALUES
    ('admin',      'Full administrative access to all APIs'),
    ('api-reader', 'Read-only access to APIs'),
    ('api-writer', 'Read/write access to APIs')
ON CONFLICT DO NOTHING;

--rollback DELETE FROM roles WHERE name IN ('admin', 'api-reader', 'api-writer');
