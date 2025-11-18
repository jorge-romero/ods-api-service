package org.opendevstack.apiservice.projectusers.exception;

/**
 * Exception thrown when an invalid token is provided.
 */
public class InvalidTokenException extends ProjectUserException {
    public InvalidTokenException(String message) {
        super(message, ErrorCodes.INVALID_TOKEN);
    }
}
