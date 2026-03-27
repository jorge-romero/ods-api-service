package org.opendevstack.apiservice.project.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.project.controller.ProjectController;
import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.exception.ProjectValidationException;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(assignableTypes = ProjectController.class)
@Slf4j
public class ProjectExceptionHandler {
    
    private static final Map<String, ErrorKey> FIELD_ERROR_MAP = Map.of(
            "projectKey", ErrorKey.PROJECT_KEY_INVALID_FORMAT,
            "projectName", ErrorKey.PROJECT_NAME_INVALID_FORMAT,
            "projectDescription", ErrorKey.PROJECT_DESCRIPTION_INVALID_FORMAT,
            "projectFlavor", ErrorKey.BAD_REQUEST_FLAVOR_CONFIG_ITEM,
            "configurationItem", ErrorKey.BAD_REQUEST_FLAVOR_CONFIG_ITEM
    );

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CreateProjectResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {

        log.warn("Request body validation error: {}", ex.getMessage());

        FieldError fieldError = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .orElse(null);

        CreateProjectResponse response = new CreateProjectResponse();
        response.setLocation(ProjectController.API_BASE_PATH);
        response.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());

        if (fieldError != null) {
            String field = fieldError.getField();

            ErrorKey key = FIELD_ERROR_MAP.getOrDefault(field, ErrorKey.BAD_REQUEST_BODY);

            response.setErrorKey(key.getKey());
            response.setMessage(key.getMessage());
        } else {
            response.setErrorKey(ErrorKey.BAD_REQUEST_BODY.getKey());
            response.setMessage(ErrorKey.BAD_REQUEST_BODY.getMessage());
        }

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ProjectValidationException.class)
    public ResponseEntity<CreateProjectResponse> handleValidationException(ProjectValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        ErrorKey errorKey = ex.getErrorKey();
        CreateProjectResponse response = new CreateProjectResponse();
        response.setLocation(ProjectController.API_BASE_PATH);
        response.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        response.setErrorKey(errorKey.getKey());
        response.setMessage(errorKey.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}


