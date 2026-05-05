package org.opendevstack.apiservice.core.security.obo;

public class OboTokenException extends RuntimeException {

    public OboTokenException(String message) {
        super(message);
    }

    public OboTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
