package org.opendevstack.apiservice.projectusers.exception;

/**
 * Exception thrown when token decoding fails.
 */
public class TokenDecodingException extends ProjectUserException {
    public TokenDecodingException(String message, Throwable cause) {
        super(message, ErrorCodes.TOKEN_DECODING_ERROR, cause);
    }
}
