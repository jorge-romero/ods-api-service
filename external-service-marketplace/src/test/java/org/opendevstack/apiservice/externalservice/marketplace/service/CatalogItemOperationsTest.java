package org.opendevstack.apiservice.externalservice.marketplace.service;

import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProjectComponentExtendedInfo;

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
        ProjectComponentExtendedInfo testComponentExtendedInfo = new ProjectComponentExtendedInfo();
        testComponentExtendedInfo.setCatalogItemId(null);
        testComponentExtendedInfo.setCatalogItemRef(null);

        String catalogItemId = CatalogItemOperations.buildCatalogItemId(testComponentExtendedInfo);
        assertNull(catalogItemId);
    }

    @Test
    void testBuildCatalogItemId_whenCorrectValues_ReturnCorrectlyBuiltId() {
        ProjectComponentExtendedInfo testComponentExtendedInfo = new ProjectComponentExtendedInfo();
        testComponentExtendedInfo.setCatalogItemId("cHJvamVjdHMvVEVTVC9yZXBvcy9DYXRhbG9nSXRlbS55YW1s");
        testComponentExtendedInfo.setCatalogItemRef("P2F0PXJlZnMvaGVhZHMvbWFzdGVy");

        String catalogItemId = CatalogItemOperations.buildCatalogItemId(testComponentExtendedInfo);
        assertNotNull(catalogItemId);
        assertEquals("cHJvamVjdHMvVEVTVC9yZXBvcy9DYXRhbG9nSXRlbS55YW1sP2F0PXJlZnMvaGVhZHMvbWFzdGVy", catalogItemId);
    }

}