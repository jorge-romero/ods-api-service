package org.opendevstack.apiservice.project.exception;

public class ErrorMessage {
    
    public static final String NOT_FOUND = "Not Found";
    public static final String FORBIDDEN = "Forbidden";
    public static final String BAD_REQUEST = "Bad Request";
    public static final String INTERNAL_ERROR = "Internal error";
    public static final String SUCCESS = "Success";
    
    public static final String INVALID_LOCATION = "Incorrect location. Valid locations are:";
    public static final String RECORD_ALREADY_EXISTS = "Record already exists";
    public static final String PROJECT_KEY_NOT_MET_THE_PATTERN = "projectKey not met the pattern ^[A-Z] {2}[A-Z0-9] {1,8}$";
    public static final String PROJECT_NAME_NOT_MET_THE_PATTERN = "projectName not met the pattern ^[A-Za-z0-9 ] {0,80}$";
    public static final String PROJECT_DESCRIPTION_NOT_MET_THE_PATTERN = "projectDescription not met the pattern ^.{0,255}$";
    public static final String PROJECT_OWNER_NOT_MET_THE_PATTERN = "projectOwner not met the pattern ^[a-z]{1,10}$";
    public static final String PROJECT_X_2_ACCOUNT_NOT_MET_THE_PATTERN = "projectX2Account not met the pattern ^x2[a-zA-Z0-9]{0,13}$";
    public static final String PROJECT_FLAVOUR_AND_CONFIG_ITEM_CANNOT_BE_BOTH_NULL = "Project flavour and config item cannot be both null";
    public static final String OWNER_MUST_BE_PRESENT_IF_THE_X_2_ACCOUNT_IS_PRESENT = "Owner must be present if the X2 account is present";
    public static final String PROJECT_ALREADY_EXISTS = "Project already exists";
    public static final String PROJECT_WITH_SAME_PROJECT_NAME_ALREADY_EXISTS = "Project with same project name already exists";
    public static final String CLIENT_APP_NOT_REGISTERED_MANUAL_REGISTRATION_REQUIRED = "ClientApp not registered, manual registration required";
    
    private ErrorMessage() {
        // prevent instantiation
    }
}
