package org.opendevstack.apiservice.project.exception;

public class CatalogItemNotFoundException extends RuntimeException {

    public CatalogItemNotFoundException(String message) {
        super(message);
    }

    public CatalogItemNotFoundException(String message, Exception e) {
        super(message, e);
    }
}
