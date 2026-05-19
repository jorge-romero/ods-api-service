package org.opendevstack.apiservice.project.controller.advice;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendevstack.apiservice.project.exception.ComponentAlreadyExistsException;
import org.opendevstack.apiservice.project.exception.ComponentCreationException;
import org.opendevstack.apiservice.project.exception.ComponentNotFoundException;
import org.opendevstack.apiservice.project.exception.ComponentRegistrationException;
import org.opendevstack.apiservice.project.model.CreateComponentResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ProjectComponentsExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private ProjectComponentsExceptionHandler handler;

    private AutoCloseable openMocks;

    @BeforeEach
    void setup() {
        openMocks = MockitoAnnotations.openMocks(this);
        handler = new ProjectComponentsExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/pub/v0/projects/test-project/components/");
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void handle_method_argument_not_valid_exception_returns_bad_request_with_regex_error_key() throws Exception {
        DummyRequest dummyRequest = new DummyRequest();
        BindingResult bindingResult = new BeanPropertyBindingResult(dummyRequest, "createComponentRequest");
        bindingResult.rejectValue("name", "Pattern", "has invalid format");

        Method method = DummyController.class.getDeclaredMethod("dummyMethod", DummyRequest.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<CreateComponentResponse> response = handler.handleMethodArgumentNotValidException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST.name());
        assertThat(response.getBody().getErrorKey()).isEqualTo("010");
        assertThat(response.getBody().getMessage()).isEqualTo("Field: name has invalid format");
        assertThat(response.getBody().getPath()).isEqualTo("/api/pub/v0/projects/test-project/components/");
    }

    @Test
    void handle_method_argument_type_mismatch_exception_returns_bad_request() throws Exception {
        Method method = DummyController.class.getDeclaredMethod("dummyMethod", DummyRequest.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "not-a-uuid",
                String.class,
                "componentId",
                methodParameter,
                new IllegalArgumentException("Invalid UUID")
        );

        ResponseEntity<CreateComponentResponse> response = handler.handleMethodArgumentTypeMismatchException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorKey()).isEqualTo("006");
        assertThat(response.getBody().getPath()).isEqualTo("/api/pub/v0/projects/test-project/components/");
    }

    @Test
    void handle_component_not_found_exception_returns_not_found() {
        ComponentNotFoundException exception = new ComponentNotFoundException("Component not found");

        ResponseEntity<CreateComponentResponse> response = handler.handleComponentNotFoundException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorKey()).isEqualTo("013");
        assertThat(response.getBody().getMessage()).isEqualTo("Component not found");
    }

    @Test
    void handle_access_denied_exception_returns_forbidden() {
        AccessDeniedException exception = new AccessDeniedException("Forbidden");

        ResponseEntity<CreateComponentResponse> response = handler.handleAccessDeniedException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorKey()).isEqualTo("002");
        assertThat(response.getBody().getMessage()).isEqualTo("Forbidden");
    }

    @Test
    void handle_http_message_not_readable_exception_returns_bad_request() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Malformed JSON");

        ResponseEntity<CreateComponentResponse> response = handler.handleHttpMessageNotReadableException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorKey()).isEqualTo("017");
        assertThat(response.getBody().getMessage()).isEqualTo("Params property should be a valid json.");
    }

    @Test
    void handle_component_creation_exception_returns_internal_server_error() {
        ComponentCreationException exception = new ComponentCreationException("Creation failed");

        ResponseEntity<CreateComponentResponse> response = handler.handleComponentCreationException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorKey()).isEqualTo("003");
        assertThat(response.getBody().getMessage()).isEqualTo("Creation failed");
    }

    @Test
    void handle_component_already_exists_exception_returns_conflict() {
        ComponentAlreadyExistsException exception = new ComponentAlreadyExistsException(
                "This component name already exists, please choose another name.");

        ResponseEntity<CreateComponentResponse> response = handler.handleComponentAlreadyExistsException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getHttpStatus()).isEqualTo(HttpStatus.CONFLICT.name());
        assertThat(response.getBody().getErrorKey()).isEqualTo("006");
        assertThat(response.getBody().getMessage()).isEqualTo("This component name already exists, please choose another name.");
    }

    @Test
    void handle_component_registration_exception_returns_internal_server_error() {
        ComponentRegistrationException exception = new ComponentRegistrationException("Registration failed");

        ResponseEntity<CreateComponentResponse> response = handler.handleComponentRegisterException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.name());
        assertThat(response.getBody().getErrorKey()).isEqualTo("003");
        assertThat(response.getBody().getMessage()).isEqualTo("Registration failed");
        assertThat(response.getBody().getPath()).isEqualTo("/api/pub/v0/projects/test-project/components/");
    }

    @Test
    void handle_generic_exception_returns_internal_server_error() {
        RuntimeException exception = new RuntimeException("boom");

        ResponseEntity<CreateComponentResponse> response = handler.handleGenericException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorKey()).isEqualTo("003");
        assertThat(response.getBody().getMessage()).isEqualTo("An error occurred while processing the request.");
    }

    private static class DummyRequest {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @SuppressWarnings("unused")
    private static class DummyController {
        public void dummyMethod(@ModelAttribute DummyRequest request) {
            // no implementation needed for testing purposes
        }
    }
}
