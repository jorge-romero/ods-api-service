package org.opendevstack.apiservice.projectusers.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UserNotAuthenticatedException extends ProjectUserException {

    public UserNotAuthenticatedException(String message, String errorCode) {
        super(message, errorCode);
    }
}
