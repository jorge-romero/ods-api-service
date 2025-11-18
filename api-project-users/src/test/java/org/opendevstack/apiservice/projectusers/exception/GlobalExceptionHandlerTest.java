package org.opendevstack.apiservice.projectusers.exception;

import org.opendevstack.apiservice.projectusers.controller.ProjectUserController;
import org.opendevstack.apiservice.projectusers.model.AddUserToProjectRequest;
import org.opendevstack.apiservice.projectusers.model.ValidationErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for the GlobalExceptionHandler to verify improved validation
 * error messages.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testValidationErrorHandling() {
        // Create a mock MethodArgumentNotValidException with validation errors
        // Target object representing the @RequestBody argument
        AddUserToProjectRequest target = new AddUserToProjectRequest();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "addUserToProjectRequest");

        // Add field errors for required fields
        bindingResult.addError(new FieldError("addUserToProjectRequest", "environment", null, false, null, null,
                "Environment cannot be blank"));
        bindingResult.addError(
                new FieldError("addUserToProjectRequest", "user", null, false, null, null, "User cannot be blank"));
        bindingResult.addError(new FieldError("addUserToProjectRequest", "account", null, false, null, null,
                "Account cannot be blank"));
        bindingResult.addError(
                new FieldError("addUserToProjectRequest", "role", null, false, null, null, "Role cannot be null"));

        // Create a MethodParameter referencing the controller method's @RequestBody
        // parameter
        MethodParameter methodParameter;
        try {
            methodParameter = new MethodParameter(
                    ProjectUserController.class.getMethod(
                            "triggerMembershipRequest", String.class, AddUserToProjectRequest.class),
                    1 // index of AddUserToProjectRequest parameter
            );
        } catch (NoSuchMethodException e) {
            fail("Failed to reflect controller method for test: " + e.getMessage());
            return; // unreachable, but required for compilation
        }

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        // Test the exception handler
        ResponseEntity<ValidationErrorResponse> response = exceptionHandler
                .handleMethodArgumentNotValidException(exception);

        // Verify response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        ValidationErrorResponse errorResponse = response.getBody();
        assertFalse(errorResponse.getSuccess());
        assertEquals("PROJECT_USER_ERROR", errorResponse.getErrorCode());
        assertNotNull(errorResponse.getFieldErrors());
        assertEquals(4, errorResponse.getFieldErrors().size());

        // Check specific field errors
        List<org.opendevstack.apiservice.projectusers.model.FieldError> fieldErrors = errorResponse.getFieldErrors();
        assertTrue(fieldErrors.stream().anyMatch(error -> "environment".equals(error.getField())));
        assertTrue(fieldErrors.stream().anyMatch(error -> "user".equals(error.getField())));
        assertTrue(fieldErrors.stream().anyMatch(error -> "account".equals(error.getField())));
        assertTrue(fieldErrors.stream().anyMatch(error -> "role".equals(error.getField())));

        // Verify expected format is provided for each field
        fieldErrors.forEach(fieldError -> {
            assertNotNull(fieldError.getField());
            assertNotNull(fieldError.getMessage());
            if (!"object".equals(fieldError.getField())) {
                assertNotNull(fieldError.getExpectedFormat(),
                        "Expected format should be provided for field: " + fieldError.getField());
            }
        });
    }

    @Test
    void testGenericExceptionHandling() {
        // Test generic exception handling
        Exception exception = new RuntimeException("Unexpected error");

        ResponseEntity<?> response = exceptionHandler.handleGenericException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}