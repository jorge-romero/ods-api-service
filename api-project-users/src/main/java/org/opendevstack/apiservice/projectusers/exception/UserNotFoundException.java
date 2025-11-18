package org.opendevstack.apiservice.projectusers.exception;

/**
 * Exception thrown when a user is not found.
 */
public class UserNotFoundException extends ProjectUserException {
    public UserNotFoundException(String user) {
        super(String.format(ErrorMessages.USER_NOT_FOUND, user), ErrorCodes.USER_NOT_FOUND);
    }
}
