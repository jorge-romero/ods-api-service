package org.opendevstack.apiservice.project.exception;

public class ClientAppNotRegisteredException extends RuntimeException {

    public ClientAppNotRegisteredException(String clientId) {
        super(String.format("ClientApp with clientId '%s' is not registered", clientId));
    }
}