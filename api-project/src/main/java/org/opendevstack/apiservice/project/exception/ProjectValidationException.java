package org.opendevstack.apiservice.project.exception;

import lombok.Getter;

@Getter
public class ProjectValidationException extends RuntimeException {

    private final ErrorKey errorKey;

    public ProjectValidationException(ErrorKey errorKey) {
        super(errorKey.getMessage());
        this.errorKey = errorKey;
    }
}

