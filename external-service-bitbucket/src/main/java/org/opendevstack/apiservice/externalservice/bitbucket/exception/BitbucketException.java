package org.opendevstack.apiservice.externalservice.bitbucket.exception;

/**
 * Exception thrown when Bitbucket API operations fail.
 */
public class BitbucketException extends Exception {
    
    public BitbucketException(String message) {
        super(message);
    }

    public BitbucketException(String message, Throwable cause) {
        super(message, cause);
    }
}
