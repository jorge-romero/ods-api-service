package org.opendevstack.apiservice.projectusers.exception;

import org.opendevstack.apiservice.projectusers.model.ValidationErrorResponse;
import org.opendevstack.apiservice.projectusers.model.BaseApiResponse;
import org.opendevstack.apiservice.projectusers.model.FieldError;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.opendevstack.apiservice.externalservice.aap.exception.AutomationPlatformException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Global exception handler for the Project Users API.
 * Provides comprehensive error handling with detailed validation error
 * messages.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles validation errors from @Valid annotations on request bodies.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {

        logger.warn("Validation failed for request: {}", ex.getMessage());

        List<FieldError> fieldErrors = new ArrayList<>();

        // Field validation errors
        for (org.springframework.validation.FieldError error : ex.getBindingResult().getFieldErrors()) {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            Object rejectedValue = error.getRejectedValue();
            String expectedFormat = errorMessage;

            FieldError fieldError = new FieldError();
            fieldError.setField(fieldName);
            fieldError.setMessage(errorMessage);
            fieldError.setRejectedValue(org.openapitools.jackson.nullable.JsonNullable.of(rejectedValue));
            fieldError.setExpectedFormat(expectedFormat);
            fieldErrors.add(fieldError);
        }

        // Global validation errors
        ex.getBindingResult().getGlobalErrors().forEach(error -> {
            FieldError fieldError = new FieldError();
            fieldError.setField("object");
            fieldError.setMessage(error.getDefaultMessage());
            fieldErrors.add(fieldError);
        });

    String errorMessage = String.format(
        ErrorMessages.REQUEST_VALIDATION_FAILED,
        fieldErrors.size());

    ValidationErrorResponse errorResponse = new ValidationErrorResponse();
    errorResponse.setSuccess(false);
    errorResponse.setMessage(errorMessage);
    errorResponse.setErrorCode(ErrorCodes.PROJECT_USER_ERROR);
    errorResponse.setFieldErrors(fieldErrors);
    errorResponse.setTimestamp(java.time.OffsetDateTime.now());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles constraint validation errors from method parameters.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseApiResponse> handleConstraintViolationException(
            ConstraintViolationException ex) {

        logger.warn("Constraint violation: {}", ex.getMessage());

        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(this::formatConstraintViolation)
                .toList();

    String errorMessage = String.format(ErrorMessages.PARAMETER_VALIDATION_FAILED, String.join("; ", errors));
    BaseApiResponse errorResponse = new BaseApiResponse();
    errorResponse.setSuccess(false);
    errorResponse.setMessage(errorMessage);
    errorResponse.setError(ErrorCodes.PROJECT_USER_ERROR);
    errorResponse.setTimestamp(java.time.OffsetDateTime.now());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles HTTP message not readable exceptions (malformed JSON, wrong data
     * types, etc.).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseApiResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {

        logger.warn("Invalid request body: {}", ex.getMessage());

    String errorMessage = ErrorMessages.INVALID_REQUEST_BODY;
    String errorCode = ErrorCodes.PROJECT_USER_ERROR;

        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException formatEx) {
            String fieldPath = getFieldPath(formatEx.getPath());
            Object invalidValue = formatEx.getValue();
            Class<?> targetType = formatEx.getTargetType();

            errorMessage = String.format(
                    ErrorMessages.INVALID_VALUE_FOR_FIELD,
                    invalidValue, fieldPath, targetType.getSimpleName());
            errorCode = ErrorCodes.INVALID_ROLE;

        } else if (cause instanceof MismatchedInputException mismatchEx) {
            String fieldPath = getFieldPath(mismatchEx.getPath());
            Class<?> targetType = mismatchEx.getTargetType();

            if (fieldPath.isEmpty()) {
                errorMessage = String.format(ErrorMessages.EXPECTED_TYPE_BUT_RECEIVED_INVALID_INPUT, targetType.getSimpleName());
            } else {
                errorMessage = String.format(
                        ErrorMessages.MISSING_OR_INVALID_VALUE_FOR_FIELD,
                        fieldPath, targetType.getSimpleName());
            }
            errorCode = ErrorCodes.PROJECT_USER_ERROR;

        } else if (cause instanceof JsonMappingException jsonEx) {
            String fieldPath = getFieldPath(jsonEx.getPath());

            if (!fieldPath.isEmpty()) {
                errorMessage = String.format(ErrorMessages.INVALID_JSON_STRUCTURE_AT_FIELD, fieldPath,
                        jsonEx.getOriginalMessage());
            } else {
                errorMessage = String.format(ErrorMessages.INVALID_JSON_STRUCTURE, jsonEx.getOriginalMessage());
            }
            errorCode = ErrorCodes.PROJECT_USER_ERROR;
        }

        BaseApiResponse errorResponse = new BaseApiResponse();
        errorResponse.setSuccess(false);
        errorResponse.setMessage(errorMessage);
        errorResponse.setError(errorCode);
        errorResponse.setTimestamp(java.time.OffsetDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles missing path variable exceptions.
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<BaseApiResponse> handleMissingPathVariableException(
            MissingPathVariableException ex) {

        logger.warn("Missing path variable: {}", ex.getMessage());

    String errorMessage = String.format(
        ErrorMessages.REQUIRED_PATH_PARAMETER_MISSING,
        ex.getVariableName());
    BaseApiResponse errorResponse = new BaseApiResponse();
    errorResponse.setSuccess(false);
    errorResponse.setMessage(errorMessage);
    errorResponse.setError(ErrorCodes.PROJECT_USER_ERROR);
    errorResponse.setTimestamp(java.time.OffsetDateTime.now());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles missing request parameter exceptions.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseApiResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex) {

        logger.warn("Missing request parameter: {}", ex.getMessage());

    String errorMessage = String.format(
        ErrorMessages.REQUIRED_REQUEST_PARAMETER_MISSING,
        ex.getParameterName(), ex.getParameterType());
    BaseApiResponse errorResponse = new BaseApiResponse();
    errorResponse.setSuccess(false);
    errorResponse.setMessage(errorMessage);
    errorResponse.setError(ErrorCodes.PROJECT_USER_ERROR);
    errorResponse.setTimestamp(java.time.OffsetDateTime.now());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles method argument type mismatch exceptions.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseApiResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {

        logger.warn("Method argument type mismatch: {}", ex.getMessage());

    String errorMessage = String.format(
        ErrorMessages.PARAMETER_TYPE_CONVERSION_FAILED,
        ex.getName(),
        ex.getValue(),
        ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
    BaseApiResponse errorResponse = new BaseApiResponse();
    errorResponse.setSuccess(false);
    errorResponse.setMessage(errorMessage);
    errorResponse.setError(ErrorCodes.INVALID_ROLE);
    errorResponse.setTimestamp(java.time.OffsetDateTime.now());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles project not found exceptions.
     */
    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<BaseApiResponse> handleProjectNotFoundException(
            ProjectNotFoundException ex) {

        logger.warn("Project not found: {}", ex.getMessage());
    BaseApiResponse errorResponse = new BaseApiResponse();
    errorResponse.setSuccess(false);
    errorResponse.setMessage(ex.getMessage());
    errorResponse.setError(ex.getErrorCode());
    errorResponse.setTimestamp(java.time.OffsetDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles user not found exceptions.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<BaseApiResponse> handleUserNotFoundException(
            UserNotFoundException ex) {

        logger.warn("User not found: {}", ex.getMessage());
    BaseApiResponse errorResponse = new BaseApiResponse();
    errorResponse.setSuccess(false);
    errorResponse.setMessage(ex.getMessage());
    errorResponse.setError(ex.getErrorCode());
    errorResponse.setTimestamp(java.time.OffsetDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles user not authenticated exceptions.
     */
    @ExceptionHandler(UserNotAuthenticatedException.class)
    public ResponseEntity<BaseApiResponse> handleUserNotAuthenticatedException(
            UserNotAuthenticatedException ex) {

        logger.warn("User not authenticated: {}", ex.getMessage());
        BaseApiResponse errorResponse = new BaseApiResponse();
        errorResponse.setSuccess(false);
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setError(ex.getErrorCode());
        errorResponse.setTimestamp(java.time.OffsetDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handles user not authorized exceptions.
     */
    @ExceptionHandler(UserNotAuthorizedException.class)
    public ResponseEntity<BaseApiResponse> handleUserNotAuthorizedException(
            UserNotAuthorizedException ex) {

        logger.warn("User not authorized: {}", ex.getMessage());
        BaseApiResponse errorResponse = new BaseApiResponse();
        errorResponse.setSuccess(false);
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setError(ex.getErrorCode());
        errorResponse.setTimestamp(java.time.OffsetDateTime.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handles invalid role exceptions.
     */
    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<BaseApiResponse> handleInvalidRoleException(
            InvalidRoleException ex) {

        logger.warn("Invalid role: {}", ex.getMessage());
    BaseApiResponse errorResponse = new BaseApiResponse();
    errorResponse.setSuccess(false);
    errorResponse.setMessage(ex.getMessage());
    errorResponse.setError(ex.getErrorCode());
    errorResponse.setTimestamp(java.time.OffsetDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles automation platform exceptions.
     */
    @ExceptionHandler(AutomationPlatformException.class)
    public ResponseEntity<BaseApiResponse> handleAutomationPlatformException(
            AutomationPlatformException ex) {

        logger.error("Automation platform error: {}", ex.getMessage(), ex);
    BaseApiResponse errorResponse = new BaseApiResponse();
    errorResponse.setSuccess(false);
    errorResponse.setMessage(String.format(ErrorMessages.EXTERNAL_SERVICE_ERROR, ex.getMessage()));
    errorResponse.setError(ex.getErrorCode());
    errorResponse.setTimestamp(java.time.OffsetDateTime.now());
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    /**
     * Handles general project user exceptions.
     */
    @ExceptionHandler(ProjectUserException.class)
    public ResponseEntity<BaseApiResponse> handleProjectUserException(ProjectUserException ex) {
        logger.error("Project user operation failed: {}", ex.getMessage(), ex);
    BaseApiResponse errorResponse = new BaseApiResponse();
    errorResponse.setSuccess(false);
    errorResponse.setMessage(String.format(ErrorMessages.OPERATION_FAILED, ex.getMessage()));
    errorResponse.setError(ex.getErrorCode());
    errorResponse.setTimestamp(java.time.OffsetDateTime.now());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handles all other unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseApiResponse> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
    BaseApiResponse errorResponse = new BaseApiResponse();
    errorResponse.setSuccess(false);
    errorResponse.setMessage(ErrorMessages.UNEXPECTED_ERROR);
    errorResponse.setError(ErrorCodes.PROJECT_USER_ERROR);
    errorResponse.setTimestamp(java.time.OffsetDateTime.now());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Formats a constraint violation into a readable error message.
     */
    private String formatConstraintViolation(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        String message = violation.getMessage();
        Object invalidValue = violation.getInvalidValue();

        if (invalidValue != null) {
            return String.format(ErrorMessages.PARAMETER_FORMAT_WITH_VALUE, propertyPath, message, invalidValue);
        } else {
            return String.format(ErrorMessages.PARAMETER_FORMAT, propertyPath, message);
        }
    }

    /**
     * Extracts field path from Jackson JsonMappingException path.
     */
    private String getFieldPath(List<JsonMappingException.Reference> path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        return path.stream()
                .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "[" + ref.getIndex() + "]")
                .collect(Collectors.joining("."));
    }
}