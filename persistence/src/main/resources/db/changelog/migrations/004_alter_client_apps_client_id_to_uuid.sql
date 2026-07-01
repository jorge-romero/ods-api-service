--liquibase formatted sql

--changeset ods:004-alter-client-apps-client-id-to-uuid
-- Description: Converts client_apps.client_id from VARCHAR(36) to UUID to align DB and Java types.
ALTER TABLE client_apps
    ALTER COLUMN client_id TYPE UUID
    USING client_id::uuid;

--rollback ALTER TABLE client_apzzps
--rollback     ALTER COLUMN client_id TYPE VARCHAR(36)
--rollback     USING client_id::text;

