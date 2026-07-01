--liquibase formatted sql

--changeset ods:002-create-client-apps
-- Description: Creates the `client_apps` table, representing registered Azure AD
--   clients that are authorised to call the service APIs.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS client_apps (
    id          UUID            NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    client_id   VARCHAR(36)     NOT NULL,
    client_name VARCHAR(255)     NOT NULL,
    enabled     BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_client_apps_client_id
    ON client_apps (client_id);

COMMENT ON TABLE  client_apps             IS 'Azure AD clients authorised to call the service APIs';
COMMENT ON COLUMN client_apps.client_id   IS 'Azure AD Application (client) UUID';
COMMENT ON COLUMN client_apps.client_name IS 'Azure AD application display name';
COMMENT ON COLUMN client_apps.enabled     IS 'When FALSE the client is denied access without removing the row';

--rollback DROP INDEX IF EXISTS uq_client_apps_client_id;
--rollback DROP TABLE IF EXISTS client_apps;


--changeset ods:002-create-client-app-project-flavors
-- Description: Each API client can be configured with one or more project flavors
--   that control how projects are created (key pattern, template, owner, etc.).
CREATE TABLE IF NOT EXISTS client_app_project_flavors (
    id                   UUID            NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    client_app_id        UUID            NOT NULL,
    name                 VARCHAR(50)     NOT NULL,
    project_key_pattern  VARCHAR(100)    NOT NULL,
    project_owner        VARCHAR(255),
    service_account      VARCHAR(255),
    config_item          VARCHAR(255),
    allowed_config_items TEXT[]          NOT NULL DEFAULT '{}',
    location             VARCHAR(50),
    created_at           TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_project_flavors_client_app
        FOREIGN KEY (client_app_id) REFERENCES client_apps (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_client_app_project_flavors_client_app_id
    ON client_app_project_flavors (client_app_id);

COMMENT ON TABLE  client_app_project_flavors                      IS 'Project flavor configurations per API client';
COMMENT ON COLUMN client_app_project_flavors.client_app_id        IS 'FK to client_apps.id (UUID)';
COMMENT ON COLUMN client_app_project_flavors.name                 IS 'Flavor name (e.g. DLSS, AMP)';
COMMENT ON COLUMN client_app_project_flavors.project_key_pattern  IS 'printf-style pattern used to generate the project key (e.g. DLSS%06d)';
COMMENT ON COLUMN client_app_project_flavors.project_owner        IS 'Default project owner username';
COMMENT ON COLUMN client_app_project_flavors.service_account      IS 'Service account associated with the flavor';
COMMENT ON COLUMN client_app_project_flavors.config_item          IS 'Default CMDB configuration item for projects created under this flavor';
COMMENT ON COLUMN client_app_project_flavors.allowed_config_items IS 'Allowed CMDB configuration item overrides (empty = no overrides permitted)';
COMMENT ON COLUMN client_app_project_flavors.location             IS 'Deployment region (e.g. eu, us)';

--rollback DROP INDEX IF EXISTS idx_client_app_project_flavors_client_app_id;
--rollback DROP TABLE IF EXISTS client_app_project_flavors;
