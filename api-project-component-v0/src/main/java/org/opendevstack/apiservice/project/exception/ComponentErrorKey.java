package org.opendevstack.apiservice.project.exception;

public enum ComponentErrorKey {

    OK("000", ComponentErrorMessage.SUCCESS),
    ACCESS_DENIED("002", ComponentErrorMessage.FORBIDDEN),
    INTERNAL_ERROR("003", ComponentErrorMessage.INTERNAL_ERROR),
    INVALID_PARAMETERS("006", ComponentErrorMessage.BAD_REQUEST),
    COMPONENT_PARAM_NOT_MEET_REGEX("010", ComponentErrorMessage.BAD_REQUEST),
    PROJECT_NOT_FOUND("012", ComponentErrorMessage.NOT_FOUND),
    COMPONENT_NOT_FOUND("013", ComponentErrorMessage.NOT_FOUND),
    BAD_REQUEST_BODY("014", ComponentErrorMessage.BAD_REQUEST),
    COMPONENT_PARAM_INVALID_FORMAT("017", ComponentErrorMessage.BAD_REQUEST);

    private final String key;
    private final String message;

    ComponentErrorKey(String key, String message) {
        this.key = key;
        this.message = message;
    }

    public String getKey() {
        return key;
    }

    public String getMessage() {
        return message;
    }
}
