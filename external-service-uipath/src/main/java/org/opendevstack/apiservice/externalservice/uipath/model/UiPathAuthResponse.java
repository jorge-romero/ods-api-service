package org.opendevstack.apiservice.externalservice.uipath.model;

import lombok.Data;

/**
 * Response model for UIPath Orchestrator authentication.
 */
@Data
public class UiPathAuthResponse {
    private String result;
    private String targetUrl;
    private boolean success;
    private String error;
    private boolean unAuthorizedRequest;

    public UiPathAuthResponse() {}

    /**
     * Get the bearer token from the result field.
     */
    public String getToken() {
        return result;
    }
}
