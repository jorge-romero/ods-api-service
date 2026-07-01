--liquibase formatted sql

--changeset ods:001-create-projects
-- Description: Creates the `projects` table, which is the central entity of the
--   ods-api-service.  Each row represents a managed project identified by
--   a unique, Atlassian project key (e.g. "MY-PROJECT").
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS projects (
    id                      UUID            NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    project_key             VARCHAR(100)    NOT NULL,
    project_name            VARCHAR(255),
    description             VARCHAR(255),
    configuration_item      VARCHAR(255)    NOT NULL,
    location                VARCHAR(50)     NOT NULL,
    project_flavor          VARCHAR(50),
    status                  VARCHAR(50),
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    ldap_group_manager      VARCHAR(255),
    ldap_group_team         VARCHAR(255),
    ldap_group_stakeholder  VARCHAR(255),
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Unique index on project_key — used as a natural FK target from other tables
CREATE UNIQUE INDEX IF NOT EXISTS uq_projects_project_key
    ON projects (project_key);

COMMENT ON TABLE  projects                          IS 'Managed projects maintained by ods-api-service';
COMMENT ON COLUMN projects.project_key              IS 'Human-readable unique identifier (e.g. MY-PROJECT)';
COMMENT ON COLUMN projects.project_name             IS 'Optional display name';
COMMENT ON COLUMN projects.description              IS 'Optional free-text description';
COMMENT ON COLUMN projects.configuration_item       IS 'CMDB configuration item identifier';
COMMENT ON COLUMN projects.location                 IS 'Deployment region; known values: cn, eu, nah, us, us-test';
COMMENT ON COLUMN projects.project_flavor           IS 'Project type classifier; known values: AMP, DLSS';
COMMENT ON COLUMN projects.status                   IS 'Provisioning status; known values: Failed, Pending (null = completed)';
COMMENT ON COLUMN projects.deleted                  IS 'Soft-delete flag';
COMMENT ON COLUMN projects.ldap_group_manager       IS 'Full LDAP CN for the PROJECT_MANAGER group';
COMMENT ON COLUMN projects.ldap_group_team          IS 'Full LDAP CN for the PROJECT_TEAM group';
COMMENT ON COLUMN projects.ldap_group_stakeholder   IS 'Full LDAP CN for the PROJECT_STAKEHOLDER group';
COMMENT ON COLUMN projects.created_at               IS 'Original creation timestamp (UTC)';
COMMENT ON COLUMN projects.updated_at               IS 'Timestamp of last update (UTC)';

--rollback DROP INDEX IF EXISTS uq_projects_project_key;
--rollback DROP TABLE IF EXISTS projects;
