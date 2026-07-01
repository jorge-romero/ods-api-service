package org.opendevstack.apiservice.externalservice.marketplace.exception;

public class MarketplaceException extends Exception {

    public MarketplaceException(String message) {
        super(message);
    }

    public MarketplaceException(String message, Throwable cause) {
        super(message, cause);
    }
}
