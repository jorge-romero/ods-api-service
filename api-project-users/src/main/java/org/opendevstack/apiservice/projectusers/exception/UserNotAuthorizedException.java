package org.opendevstack.apiservice.projectusers.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UserNotAuthorizedException extends ProjectUserException {

    public UserNotAuthorizedException(String message, String errorCode) {
        super(message, errorCode);
    }
}
