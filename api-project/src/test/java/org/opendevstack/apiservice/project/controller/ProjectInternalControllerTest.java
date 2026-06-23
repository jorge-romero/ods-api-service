package org.opendevstack.apiservice.project.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.exception.ProjectValidationException;
import org.opendevstack.apiservice.project.facade.ProjectsFacade;
import org.opendevstack.apiservice.project.model.UpdateProjectRequest;
import org.opendevstack.apiservice.project.validation.ProjectRequestValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectInternalControllerTest {

    @Mock
    private ProjectsFacade projectsFacade;

    @Mock
    private ProjectRequestValidator projectRequestValidator;

    private ProjectInternalController sut;
    private AutoCloseable mocks;

    @BeforeEach
    void setup() {
        mocks = MockitoAnnotations.openMocks(this);
        sut = new ProjectInternalController(projectsFacade, projectRequestValidator);

        Jwt jwtToken = Jwt.withTokenValue("dummy-token")
                .claim("appid", UUID.randomUUID().toString())
                .claim("sub", "test-user")
                .header("alg", "none")
                .build();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwtToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void update_project_returns_no_content() {
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setStatus("Running");

        ResponseEntity<Void> result = sut.updateProject("PROJ01", request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();
        assertThat(result.getHeaders().getFirst("Location"))
                .isEqualTo(ProjectInternalController.API_BASE_PATH + "/PROJ01");
        verify(projectRequestValidator).validateUpdateRequest(request);
        verify(projectsFacade).updateProject("PROJ01", request);
    }

    @Test
    void update_project_propagates_validation_exception_when_validator_throws() {
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setStatus("INVALID_STATUS");

        doThrow(new ProjectValidationException(ErrorKey.INVALID_STATUS))
                .when(projectRequestValidator).validateUpdateRequest(request);

        assertThrows(ProjectValidationException.class, () -> sut.updateProject("PROJ01", request));

        verify(projectRequestValidator).validateUpdateRequest(request);
        verify(projectsFacade, never()).updateProject("PROJ01", request);
    }
}
