package org.opendevstack.apiservice.externalservice.marketplace.service;

import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProjectComponentExtendedInfo;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CatalogItemOperations {

    private CatalogItemOperations() {
    }

    public static byte[] encodeId(String id) {
        return Base64.getUrlEncoder().encode(id.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] decodeId(String id) {
        return Base64.getUrlDecoder().decode(id);
    }

    public static String buildCatalogItemId(ProjectComponentExtendedInfo component) {
        if (component == null || component.getCatalogItemId() == null || component.getCatalogItemRef() == null) {
            return null;
        }
        String decodedId = new String(decodeId(component.getCatalogItemId()), StandardCharsets.UTF_8);
        String decodedRef = new String(decodeId(component.getCatalogItemRef()), StandardCharsets.UTF_8);
        return new String(encodeId(decodedId + decodedRef), StandardCharsets.UTF_8);
    }
}
