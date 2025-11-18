package org.opendevstack.apiservice.projectusers.exception;

/**
 * Exception thrown when token creation fails.
 */
public class TokenCreationException extends ProjectUserException {
    public TokenCreationException(String message, Throwable cause) {
        super(message, ErrorCodes.TOKEN_CREATION_ERROR, cause);
    }
}
