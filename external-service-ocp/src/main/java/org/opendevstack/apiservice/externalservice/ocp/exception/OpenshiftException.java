package org.opendevstack.apiservice.externalservice.ocp.exception;

public class OpenshiftException extends Exception {
    public OpenshiftException(String message) {
        super(message);
    }

    public OpenshiftException(String message, Throwable cause) {
        super(message, cause);
    }
}
