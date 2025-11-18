package org.opendevstack.apiservice.externalservice.uipath.model;

import lombok.Data;

/**
 * Request model for UIPath Orchestrator authentication.
 */
@Data
public class UiPathAuthRequest {
    private String tenancyName;
    private String usernameOrEmailAddress;
    private String password;

    public UiPathAuthRequest() {}

    public UiPathAuthRequest(String tenancyName, String usernameOrEmailAddress, String password) {
        this.tenancyName = tenancyName;
        this.usernameOrEmailAddress = usernameOrEmailAddress;
        this.password = password;
    }
}
