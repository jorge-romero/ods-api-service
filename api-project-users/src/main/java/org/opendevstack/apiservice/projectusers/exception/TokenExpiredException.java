package org.opendevstack.apiservice.projectusers.exception;

/**
 * Exception thrown when a token has expired.
 */
public class TokenExpiredException extends ProjectUserException {
    public TokenExpiredException(String message) {
        super(message, ErrorCodes.TOKEN_EXPIRED);
    }
}
