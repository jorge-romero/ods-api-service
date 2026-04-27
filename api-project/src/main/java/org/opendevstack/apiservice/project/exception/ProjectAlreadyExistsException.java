package org.opendevstack.apiservice.project.exception;

import lombok.Getter;

@Getter
public class ProjectAlreadyExistsException extends RuntimeException {
    
    private ErrorKey errorKey;

    public ProjectAlreadyExistsException() {
        super();
    }
    
    public ProjectAlreadyExistsException(ErrorKey errorKey) {
        super(errorKey.getMessage());
        this.errorKey = errorKey;
    }
}

