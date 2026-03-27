package org.opendevstack.apiservice.project.controller.advice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockitoAnnotations;
import org.opendevstack.apiservice.project.controller.ProjectController;
import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.exception.ProjectValidationException;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProjectExceptionHandlerTest {

    private ProjectExceptionHandler sut;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        sut = new ProjectExceptionHandler();
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void handle_validation_exception_returns_bad_request_response_for_project_key_invalid_format() {
        ProjectValidationException exception = new ProjectValidationException(ErrorKey.PROJECT_KEY_INVALID_FORMAT);

        ResponseEntity<CreateProjectResponse> result = sut.handleValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(ProjectController.API_BASE_PATH, result.getBody().getLocation());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), result.getBody().getError());
        assertEquals("018", result.getBody().getErrorKey());
        assertEquals("projectKey not met the pattern ^[A-Z] {2}[A-Z0-9] {1,8}$", result.getBody().getMessage());
        assertNull(result.getBody().getProjectKey());
        assertNull(result.getBody().getStatus());
        assertNull(result.getBody().getErrorDescription());
    }

    @Test
    void handle_validation_exception_returns_bad_request_response_for_project_name_invalid_format() {
        ProjectValidationException exception = new ProjectValidationException(ErrorKey.PROJECT_NAME_INVALID_FORMAT);

        ResponseEntity<CreateProjectResponse> result = sut.handleValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(ProjectController.API_BASE_PATH, result.getBody().getLocation());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), result.getBody().getError());
        assertEquals("019", result.getBody().getErrorKey());
        assertEquals("projectName not met the pattern ^[A-Za-z0-9 ] {0,80}$", result.getBody().getMessage());
        assertNull(result.getBody().getProjectKey());
        assertNull(result.getBody().getStatus());
        assertNull(result.getBody().getErrorDescription());
    }

    @Test
    void handle_validation_exception_returns_bad_request_response_for_missing_flavor_and_config_item() {
        ProjectValidationException exception = new ProjectValidationException(ErrorKey.BAD_REQUEST_FLAVOR_CONFIG_ITEM);

        ResponseEntity<CreateProjectResponse> result = sut.handleValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(ProjectController.API_BASE_PATH, result.getBody().getLocation());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), result.getBody().getError());
        assertEquals("023", result.getBody().getErrorKey());
        assertEquals("Project flavour and config item cannot be both null", result.getBody().getMessage());
        assertNull(result.getBody().getProjectKey());
        assertNull(result.getBody().getStatus());
        assertNull(result.getBody().getErrorDescription());
    }

    @Test
    void handle_method_argument_not_valid_exception_returns_bad_request_response_for_request_body_validation_errors() {
        CreateProjectRequest target = new CreateProjectRequest();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "createProjectRequest");
        bindingResult.addError(new FieldError("createProjectRequest", "projectName", null, false, null, null,
                "must not be null"));

        MethodParameter methodParameter;
        try {
            methodParameter = new MethodParameter(
                    ProjectController.class.getMethod("createProject", CreateProjectRequest.class),
                    0);
        } catch (NoSuchMethodException e) {
            fail("Failed to reflect controller method for test: " + e.getMessage());
            return;
        }

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<CreateProjectResponse> result = sut.handleMethodArgumentNotValidException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(ProjectController.API_BASE_PATH, result.getBody().getLocation());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), result.getBody().getError());
        assertEquals("019", result.getBody().getErrorKey());
        assertEquals("projectName not met the pattern ^[A-Za-z0-9 ] {0,80}$", result.getBody().getMessage());
        assertNull(result.getBody().getProjectKey());
        assertNull(result.getBody().getStatus());
        assertNull(result.getBody().getErrorDescription());
    }

    @ParameterizedTest
    @MethodSource("provideValidationCases")
    void handle_method_argument_not_valid_exception_parameterized(
            String failingField,
            String expectedErrorKey,
            String expectedMessage
    ) {
        CreateProjectRequest target = new CreateProjectRequest();
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(target, "createProjectRequest");

        bindingResult.addError(new FieldError(
                "createProjectRequest",
                failingField,
                null,
                false,
                null,
                null,
                "invalid"
        ));

        MethodParameter methodParameter;
        try {
            methodParameter = new MethodParameter(
                    ProjectController.class.getMethod("createProject", CreateProjectRequest.class),
                    0);
        } catch (NoSuchMethodException e) {
            fail("Reflection error: " + e.getMessage());
            return;
        }

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<CreateProjectResponse> result =
                sut.handleMethodArgumentNotValidException(exception);

        CreateProjectResponse body = result.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(body);

        assertEquals(ProjectController.API_BASE_PATH, body.getLocation());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), body.getError());

        assertEquals(expectedErrorKey, body.getErrorKey());
        assertEquals(expectedMessage, body.getMessage());
    }
    
    private static Stream<Arguments> provideValidationCases() {
        return Stream.of(
                Arguments.of(
                        "projectName",
                        "019",
                        "projectName not met the pattern ^[A-Za-z0-9 ] {0,80}$"
                ),
                Arguments.of(
                        "projectKey",
                        "018",
                        "projectKey not met the pattern ^[A-Z] {2}[A-Z0-9] {1,8}$"
                ),
                Arguments.of(
                        "projectDescription",
                        "020",
                        "projectDescription not met the pattern ^.{0,255}$"
                ),
                Arguments.of(
                        "unknownField",
                        "014",
                        "Bad Request"
                )
        );
    }    
}

