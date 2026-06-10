package org.opendevstack.apiservice.externalservice.marketplace.service;

import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProjectComponentProvisionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CatalogItemOperationsTest {

    @Test
    void testBuildCatalogItemId_whenNullComponent_ReturnNull() {
        String catalogItemId = CatalogItemOperations.buildCatalogItemId(null);
        assertNull(catalogItemId);
    }

    @Test
    void testBuildCatalogItemId_whenNullValues_ReturnNull() {
        ProjectComponentProvisionStatus testComponentProvisionStatus = new ProjectComponentProvisionStatus();
        testComponentProvisionStatus.setCatalogItemId(null);
        testComponentProvisionStatus.setCatalogItemRef(null);

        String catalogItemId = CatalogItemOperations.buildCatalogItemId(testComponentProvisionStatus);
        assertNull(catalogItemId);
    }

    @Test
    void testBuildCatalogItemId_whenCorrectValues_ReturnCorrectlyBuiltId() {
        ProjectComponentProvisionStatus testComponentProvisionStatus = new ProjectComponentProvisionStatus();
        testComponentProvisionStatus.setCatalogItemId("cHJvamVjdHMvVEVTVC9yZXBvcy9DYXRhbG9nSXRlbS55YW1s");
        testComponentProvisionStatus.setCatalogItemRef("P2F0PXJlZnMvaGVhZHMvbWFzdGVy");

        String catalogItemId = CatalogItemOperations.buildCatalogItemId(testComponentProvisionStatus);
        assertNotNull(catalogItemId);
        assertEquals("cHJvamVjdHMvVEVTVC9yZXBvcy9DYXRhbG9nSXRlbS55YW1sP2F0PXJlZnMvaGVhZHMvbWFzdGVy", catalogItemId);
    }

}