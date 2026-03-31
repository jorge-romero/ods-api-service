package org.opendevstack.apiservice.project.exception;

public enum ErrorKey {

    OK("000", "Success"),
    PRODUCT_NOT_FOUND("001", ErrorMessage.NOT_FOUND),
    ACCESS_DENIED("002", ErrorMessage.FORBIDDEN),
    INTERNAL_ERROR("003", "Internal error"),
    INVALID_AUTH_HEADER("004", ErrorMessage.BAD_REQUEST),
    MISSING_AUTH_HEADER("005", ErrorMessage.BAD_REQUEST),
    INVALID_PARAMETERS("006", ErrorMessage.BAD_REQUEST),
    X2_ACCOUNT_MISSING_GROUPS("007", ErrorMessage.NOT_FOUND),
    ONLY_INVITED_PROJECT("008", ErrorMessage.FORBIDDEN),
    ONCE_PER_PROJECT("009", ErrorMessage.FORBIDDEN),
    COMPONENT_PARAM_NOT_MEET_REGEX("010", ErrorMessage.BAD_REQUEST),
    INVALID_LOCATION("011", ErrorMessage.BAD_REQUEST),
    PROJECT_NOT_FOUND("012", ErrorMessage.NOT_FOUND),
    COMPONENT_NOT_FOUND("013", ErrorMessage.NOT_FOUND),
    BAD_REQUEST_BODY("014", ErrorMessage.BAD_REQUEST),
    FORBIDDEN("015", ErrorMessage.FORBIDDEN),
    DUPLICATE_RECORD("016", "Record already exists"),
    COMPONENT_PARAM_INVALID_FORMAT("017", ErrorMessage.BAD_REQUEST),
    PROJECT_KEY_INVALID_FORMAT("018", "projectKey not met the pattern ^[A-Z] {2}[A-Z0-9] {1,8}$"),
    PROJECT_NAME_INVALID_FORMAT("019", "projectName not met the pattern ^[A-Za-z0-9 ] {0,80}$"),
    PROJECT_DESCRIPTION_INVALID_FORMAT("020", "projectDescription not met the pattern ^.{0,255}$"),
    PROJECT_OWNER_INVALID_FORMAT("021", "projectOwner not met the pattern ^[a-z]{1,10}$"),
    PROJECT_X2ACCOUNT_INVALID_FORMAT("022", "projectX2Account not met the pattern ^x2[a-zA-Z0-9]{0,13}$"),
    BAD_REQUEST_FLAVOR_CONFIG_ITEM("023", "Project flavour and config item cannot be both null"),
    MANDATORY_OWNER("024", "Owner must be present if the X2 account is present"),
    PROJECT_ALREADY_EXISTS("025", "Project already exists"),
    PROJECT_SAME_PROJECT_NAME_ALREADY_EXISTS("026", "Project with same project name already exists"),
    CLIENT_APP_NOT_REGISTERED("027", "ClientApp not registered, manual registration required"),
    INVALID_PROJECT_FLAVOR("028", ErrorMessage.BAD_REQUEST),
    INVALID_CONFIG_ITEM("029", ErrorMessage.BAD_REQUEST);
    
    private String key;
    private String message;

    ErrorKey(String key, String message) {
        this.key = key;
        this.message = message;
    }

    public String getKey() {
        return this.key;
    }

    public String getMessage() {
        return this.message;
    }
}
