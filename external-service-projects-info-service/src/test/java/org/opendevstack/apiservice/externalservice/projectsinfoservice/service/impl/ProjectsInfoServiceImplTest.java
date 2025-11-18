package org.opendevstack.apiservice.externalservice.projectsinfoservice.service.impl;

import org.opendevstack.apiservice.externalservice.projectsinfoservice.dto.ProjectPlatformsMother;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.dto.SectionMother;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.exception.ProjectsInfoServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.facade.ProjectsFacade;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ProjectsInfoServiceImpl.
 * Tests all methods with various scenarios including success cases, error cases, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
class ProjectsInfoServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ProjectsFacade projectsFacade;

    @Captor
    private ArgumentCaptor<HttpEntity<Void>> httpEntityCaptor;

    @Captor
    private ArgumentCaptor<String> urlCaptor;

    private ProjectsInfoServiceImpl projectsInfoService;

    private static final String BASE_URL = "http://localhost:8080/api/v2";
    private static final String PROJECT_KEY = "TEST-PROJECT";

    @BeforeEach
    void setUp() {
        projectsInfoService = new ProjectsInfoServiceImpl(restTemplate, projectsFacade);
        ReflectionTestUtils.setField(projectsInfoService, "baseUrl", BASE_URL);
    }

    // ========== Tests for getProjectPlatforms ==========

    @Test
    void testGetProjectPlatforms_Success() throws Exception {
        // given
        var section = SectionMother.of();
        var sections = List.of(section);
        var projectPlatforms = ProjectPlatformsMother.of(sections);

        when(projectsFacade.getProjectPlatforms(PROJECT_KEY)).thenReturn(projectPlatforms);

        // when
        var platforms = projectsInfoService.getProjectPlatforms(PROJECT_KEY);

        // then
        assertThat(platforms).isNotNull();

        var resultSections = platforms.getSections();
        assertThat(resultSections).isNotNull().hasSize(1);

        var resultSection  = resultSections.get(0);

        assertThat(resultSection).isNotNull();
        assertThat(resultSection.getSection()).isEqualTo(section.getSection());
        assertThat(resultSection.getTooltip()).isEqualTo(section.getTooltip());

    }

    // ========== Tests for validateConnection ==========

    @Test
    void testValidateConnection_Success() {
        // Arrange
        Map<String, Object> healthResponse = Map.of("status", "UP");
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(healthResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(new ParameterizedTypeReference<Map<String, Object>>() {})
        )).thenReturn(responseEntity);

        // Act
        boolean result = projectsInfoService.validateConnection();

        // Assert
        assertTrue(result);

        // Verify the correct URL was called
        verify(restTemplate).exchange(
                urlCaptor.capture(),
                eq(HttpMethod.GET),
                httpEntityCaptor.capture(),
                eq(new ParameterizedTypeReference<Map<String, Object>>() {})
        );

        String capturedUrl = urlCaptor.getValue();
        assertEquals(BASE_URL + "/actuator/health", capturedUrl);
    }

    @Test
    void testValidateConnection_Non2xxResponse() {
        // Arrange
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(new ParameterizedTypeReference<Map<String, Object>>() {})
        )).thenReturn(responseEntity);

        // Act
        boolean result = projectsInfoService.validateConnection();

        // Assert
        assertFalse(result);
    }

    @Test
    void testValidateConnection_Exception() {
        // Arrange
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(new ParameterizedTypeReference<Map<String, Object>>() {})
        )).thenThrow(new RestClientException("Connection refused"));

        // Act
        boolean result = projectsInfoService.validateConnection();

        // Assert
        assertFalse(result);
    }

    @Test
    void testValidateConnection_RuntimeException() {
        // Arrange
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(new ParameterizedTypeReference<Map<String, Object>>() {})
        )).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        boolean result = projectsInfoService.validateConnection();

        // Assert
        assertFalse(result);
    }

    // ========== Tests for isHealthy ==========

    @Test
    void testIsHealthy_Success() {
        // Arrange
        Map<String, Object> healthResponse = Map.of("status", "UP");
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(healthResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(new ParameterizedTypeReference<Map<String, Object>>() {})
        )).thenReturn(responseEntity);

        // Act
        boolean result = projectsInfoService.isHealthy();

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsHealthy_Failure() {
        // Arrange
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(new ParameterizedTypeReference<Map<String, Object>>() {})
        )).thenReturn(responseEntity);

        // Act
        boolean result = projectsInfoService.isHealthy();

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsHealthy_Exception() {
        // Arrange
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(new ParameterizedTypeReference<Map<String, Object>>() {})
        )).thenThrow(new RestClientException("Connection timeout"));

        // Act
        boolean result = projectsInfoService.isHealthy();

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsHealthy_RuntimeException() {
        // Arrange
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(new ParameterizedTypeReference<Map<String, Object>>() {})
        )).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        boolean result = projectsInfoService.isHealthy();

        // Assert
        assertFalse(result);
    }

    // ========== Helper methods ==========

    private Map<String, Object> createValidProjectPlatformsResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("disabledPlatforms", java.util.List.of("platform1", "platform2"));

        Map<String, String> platformLinks = new HashMap<>();
        platformLinks.put("jenkins", "https://jenkins.example.com/job/test-project");
        platformLinks.put("sonar", "https://sonar.example.com/dashboard?id=test-project");
        response.put("platformLinks", platformLinks);

        return response;
    }

    private Map<String, Object> createComplexProjectPlatformsResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("disabledPlatforms", java.util.List.of("platform1"));

        Map<String, String> platformLinks = new HashMap<>();
        platformLinks.put("jenkins", "https://jenkins.example.com/job/test-project");
        response.put("platformLinks", platformLinks);

        // Add sections
        Map<String, Object> section1 = new HashMap<>();
        section1.put("section", "CI/CD");

        Map<String, Object> link1 = new HashMap<>();
        link1.put("name", "Build Pipeline");
        link1.put("url", "https://jenkins.example.com/job/build");
        section1.put("links", java.util.List.of(link1));

        response.put("sections", java.util.List.of(section1));

        return response;
    }
}
