package org.opendevstack.apiservice.project.controller.advice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.exception.ProjectUpdateValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ProjectInternalExceptionHandlerTest {

    private ProjectInternalExceptionHandler sut;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        sut = new ProjectInternalExceptionHandler();
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void handle_update_validation_exception_returns_bad_request_with_no_body() {
        ProjectUpdateValidationException exception =
                new ProjectUpdateValidationException(ErrorKey.INVALID_STATUS);

        ResponseEntity<Void> result = sut.handleUpdateValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void handle_update_validation_exception_with_additional_message_returns_bad_request_with_no_body() {
        ProjectUpdateValidationException exception =
                new ProjectUpdateValidationException(ErrorKey.INVALID_STATUS, "Pending,Running,Failed");

        ResponseEntity<Void> result = sut.handleUpdateValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNull(result.getBody());
    }

}
