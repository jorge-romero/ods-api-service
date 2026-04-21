package org.opendevstack.apiservice.project.controller.advice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.project.controller.ComponentsResponseFactory;
import org.opendevstack.apiservice.project.controller.ProjectComponentsController;
import org.opendevstack.apiservice.project.exception.ComponentCreationException;
import org.opendevstack.apiservice.project.exception.ComponentErrorKey;
import org.opendevstack.apiservice.project.exception.ComponentNotFoundException;
import org.opendevstack.apiservice.project.model.CreateComponentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@RestControllerAdvice(assignableTypes = ProjectComponentsController.class)
@Slf4j
public class ProjectComponentsExceptionHandler {

    private static final Map<String, ComponentErrorKey> FIELD_ERROR_MAP = Map.of(
            "name", ComponentErrorKey.COMPONENT_PARAM_NOT_MEET_REGEX,
            "productId", ComponentErrorKey.INVALID_PARAMETERS,
            "params", ComponentErrorKey.INVALID_PARAMETERS
    );

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CreateComponentResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        log.warn("Request body validation error: {}", ex.getMessage());

        FieldError fieldError = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .orElse(null);

        ComponentErrorKey errorKey = ComponentErrorKey.INVALID_PARAMETERS;
        String message = ComponentErrorKey.INVALID_PARAMETERS.getMessage();

        if (fieldError != null) {
            errorKey = FIELD_ERROR_MAP.getOrDefault(fieldError.getField(), ComponentErrorKey.INVALID_PARAMETERS);
            message = String.format("Field: %s %s", fieldError.getField(), fieldError.getDefaultMessage());
        }

        CreateComponentResponse response = ComponentsResponseFactory.badRequest(
                request.getRequestURI(),
                message,
                errorKey
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CreateComponentResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        log.warn("Request parameter validation error: {}", ex.getMessage());

        CreateComponentResponse response = ComponentsResponseFactory.badRequest(
                request.getRequestURI(),
                ex.getMessage(),
                ComponentErrorKey.INVALID_PARAMETERS
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ComponentNotFoundException.class)
    public ResponseEntity<CreateComponentResponse> handleComponentNotFoundException(
            ComponentNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Component not found: {}", ex.getMessage());

        CreateComponentResponse response = ComponentsResponseFactory.notFound(
                request.getRequestURI(),
                ex.getMessage(),
                ComponentErrorKey.COMPONENT_NOT_FOUND
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CreateComponentResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("Access denied: {}", ex.getMessage());

        CreateComponentResponse response = ComponentsResponseFactory.forbidden(
                request.getRequestURI(),
                ex.getMessage(),
                ComponentErrorKey.ACCESS_DENIED
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CreateComponentResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.warn("Request body format error: {}", ex.getMessage());

        CreateComponentResponse response = ComponentsResponseFactory.badRequest(
                request.getRequestURI(),
                "Params property should be a valid json.",
                ComponentErrorKey.COMPONENT_PARAM_INVALID_FORMAT
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ComponentCreationException.class)
    public ResponseEntity<CreateComponentResponse> handleComponentCreationException(
            ComponentCreationException ex,
            HttpServletRequest request) {

        log.error("Component creation failed: {}", ex.getMessage(), ex);

        CreateComponentResponse response = ComponentsResponseFactory.internalError(
                request.getRequestURI(),
                ex.getMessage(),
                ComponentErrorKey.INTERNAL_ERROR
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CreateComponentResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error: {}", ex.getMessage(), ex);

        CreateComponentResponse response = ComponentsResponseFactory.internalError(
                request.getRequestURI(),
                "An error occurred while processing the request.",
                ComponentErrorKey.INTERNAL_ERROR
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
