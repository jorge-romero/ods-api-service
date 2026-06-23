package org.opendevstack.apiservice.project.exception;

import lombok.Getter;

@Getter
public class ProjectUpdateValidationException extends RuntimeException {

    private final ErrorKey errorKey;

    public ProjectUpdateValidationException(ErrorKey errorKey) {
        super(errorKey.getMessage());
        this.errorKey = errorKey;
    }

    public ProjectUpdateValidationException(ErrorKey errorKey, String additionalMessage) {
        super(errorKey.getMessage() + " " + additionalMessage);
        this.errorKey = errorKey;
    }
}
