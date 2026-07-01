package org.opendevstack.apiservice.project.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.project.controller.ProjectInternalController;
import org.opendevstack.apiservice.project.exception.ProjectUpdateValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ProjectInternalController.class)
@Slf4j
public class ProjectInternalExceptionHandler {

    @ExceptionHandler(ProjectUpdateValidationException.class)
    public ResponseEntity<Void> handleUpdateValidationException(ProjectUpdateValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }


}
