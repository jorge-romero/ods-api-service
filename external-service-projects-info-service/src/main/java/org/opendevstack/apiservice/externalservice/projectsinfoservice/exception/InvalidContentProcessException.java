package org.opendevstack.apiservice.externalservice.projectsinfoservice.exception;

public class InvalidContentProcessException extends RuntimeException {
    public InvalidContentProcessException(String message) {
        super(message);
    }

    public InvalidContentProcessException(String message, Exception cause) {
        super(message, cause);
    }
}
