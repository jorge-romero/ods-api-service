package org.opendevstack.apiservice.project.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.aap.exception.AutomationPlatformException;
import org.opendevstack.apiservice.project.controller.ProjectController;
import org.opendevstack.apiservice.project.exception.ClientAppNotRegisteredException;
import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.exception.ProjectCreationException;
import org.opendevstack.apiservice.project.exception.ProjectValidationException;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
            "configurationItem", ErrorKey.BAD_REQUEST_FLAVOR_CONFIG_ITEM,
            "x2OdsAccount", ErrorKey.PROJECT_X2ACCOUNT_INVALID_FORMAT,
            "owner", ErrorKey.PROJECT_OWNER_INVALID_FORMAT
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

    @ExceptionHandler(ClientAppNotRegisteredException.class)
    public ResponseEntity<CreateProjectResponse> handleClientAppNotRegisteredException(
            ClientAppNotRegisteredException ex) {
        log.warn("ClientApp registration error: {}", ex.getMessage());
        CreateProjectResponse response = new CreateProjectResponse();
        response.setLocation(ProjectController.API_BASE_PATH);
        response.setError(HttpStatus.FORBIDDEN.getReasonPhrase());
        response.setErrorKey(ErrorKey.CLIENT_APP_NOT_REGISTERED.getKey());
        response.setMessage(ErrorKey.CLIENT_APP_NOT_REGISTERED.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(ProjectValidationException.class)
    public ResponseEntity<CreateProjectResponse> handleValidationException(ProjectValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        ErrorKey errorKey = ex.getErrorKey();
        CreateProjectResponse response = new CreateProjectResponse();
        response.setLocation(ProjectController.API_BASE_PATH);
        response.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        response.setErrorKey(errorKey.getKey());
        response.setMessage(ex.getMessage()); // Needs to get the full message with additional info provided.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ProjectCreationException.class)
    public ResponseEntity<CreateProjectResponse> handleProjectCreationException(
            ProjectCreationException ex) {
        log.error("Project creation error: {}", ex.getMessage(), ex);
        CreateProjectResponse response = new CreateProjectResponse();
        response.setLocation(ProjectController.API_BASE_PATH);
        response.setError(ErrorKey.INTERNAL_ERROR.getMessage());
        response.setErrorKey(ErrorKey.INTERNAL_ERROR.getKey());
        response.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(AutomationPlatformException.class)
    public ResponseEntity<CreateProjectResponse> handleAutomationPlatformException(
            AutomationPlatformException ex) {
        log.error("Failed to execute automated job: {}", ex.getMessage(), ex);
        CreateProjectResponse response = new CreateProjectResponse();
        response.setLocation(ProjectController.API_BASE_PATH);
        response.setError(ErrorKey.INTERNAL_ERROR.getMessage());
        response.setErrorKey(ErrorKey.INTERNAL_ERROR.getKey());
        response.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CreateProjectResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        CreateProjectResponse response = new CreateProjectResponse();
        response.setLocation(ProjectController.API_BASE_PATH);
        response.setError(ErrorKey.BAD_REQUEST_BODY.getMessage());
        response.setErrorKey(ErrorKey.BAD_REQUEST_BODY.getKey());
        response.setMessage("An error occurred while processing the request.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CreateProjectResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        CreateProjectResponse response = new CreateProjectResponse();
        response.setLocation(ProjectController.API_BASE_PATH);
        response.setError(ErrorKey.INTERNAL_ERROR.getMessage());
        response.setErrorKey(ErrorKey.INTERNAL_ERROR.getKey());
        response.setMessage("An error occurred while processing the request.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}


