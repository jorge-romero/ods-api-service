package org.opendevstack.apiservice.externalservice.aap.service.impl;

import org.opendevstack.apiservice.externalservice.aap.exception.AutomationPlatformException;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationExecutionResult;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationJobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AnsibleAutomationPlatformService.
 * Tests all methods with various scenarios including success cases, error cases, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class AnsibleAutomationPlatformServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private AnsibleAutomationPlatformService service;

    @Captor
    private ArgumentCaptor<HttpEntity<Map<String, Object>>> httpEntityCaptor;

    @Captor
    private ArgumentCaptor<HttpEntity<Void>> httpEntityVoidCaptor;

    private static final String BASE_URL = "http://localhost:8080/api/v2";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass";
    private static final int TIMEOUT = 30000;

    @BeforeEach
    void setUp() {
        service = new AnsibleAutomationPlatformService(restTemplate);
        ReflectionTestUtils.setField(service, "baseUrl", BASE_URL);
        ReflectionTestUtils.setField(service, "username", USERNAME);
        ReflectionTestUtils.setField(service, "password", PASSWORD);
        ReflectionTestUtils.setField(service, "timeoutMs", TIMEOUT);
    }

    @Test
    void executeWorkflow_Success() throws AutomationPlatformException {
        // Arrange
        String workflowName = "test-workflow";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("env", "dev");
        parameters.put("region", "us-east-1");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", "12345");
        responseBody.put("status", "pending");
        responseBody.put("url", BASE_URL + "/workflow_jobs/12345/");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        AutomationExecutionResult result = service.executeWorkflow(workflowName, parameters);

        // Assert
        assertNotNull(result);
        assertEquals("12345", result.getJobId());
        assertEquals("pending", result.getStatus());
        assertTrue(result.isSuccessful());
        assertEquals("Workflow executed successfully", result.getMessage());
        assertNotNull(result.getMetadata());
        assertEquals(responseBody, result.getMetadata());

        // Verify REST call
        verify(restTemplate).postForEntity(
                eq(BASE_URL + "/workflow_job_templates/" + workflowName + "/launch/"),
                httpEntityCaptor.capture(),
                eq(Map.class)
        );

        HttpEntity<Map<String, Object>> capturedEntity = httpEntityCaptor.getValue();
        HttpHeaders headers = capturedEntity.getHeaders();
        assertEquals("application/json", headers.getFirst("Content-Type"));
        assertNotNull(headers.getFirst(HttpHeaders.AUTHORIZATION));

        Map<String, Object> requestBody = capturedEntity.getBody();
        assertNotNull(requestBody);
        assertEquals(parameters, requestBody.get("extra_vars"));
    }

    @Test
    void executeWorkflow_WithNullParameters() throws AutomationPlatformException {
        // Arrange
        String workflowName = "test-workflow";
        Map<String, Object> parameters = null;

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", "12345");
        responseBody.put("status", "pending");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        AutomationExecutionResult result = service.executeWorkflow(workflowName, parameters);

        // Assert
        assertNotNull(result);
        assertEquals("12345", result.getJobId());
        assertTrue(result.isSuccessful());
    }

    @Test
    void executeWorkflow_RestClientException() {
        // Arrange
        String workflowName = "test-workflow";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("env", "dev");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("Connection timeout"));

        // Act & Assert
        AutomationPlatformException.WorkflowExecutionException exception = assertThrows(
                AutomationPlatformException.WorkflowExecutionException.class,
                () -> service.executeWorkflow(workflowName, parameters)
        );

        assertTrue(exception.getMessage().contains(workflowName));
        assertEquals("WORKFLOW_EXECUTION_FAILED", exception.getErrorCode());
    }

    @Test
    void executeWorkflow_UnexpectedResponseStatus() {
        // Arrange
        String workflowName = "test-workflow";
        Map<String, Object> parameters = new HashMap<>();

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act & Assert
        AutomationPlatformException.WorkflowExecutionException exception = assertThrows(
                AutomationPlatformException.WorkflowExecutionException.class,
                () -> service.executeWorkflow(workflowName, parameters)
        );

        assertTrue(exception.getMessage().contains("Unexpected response status"));
    }

    @Test
    void executeWorkflow_NullResponseBody() {
        // Arrange
        String workflowName = "test-workflow";
        Map<String, Object> parameters = new HashMap<>();

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act & Assert
        AutomationPlatformException.WorkflowExecutionException exception = assertThrows(
                AutomationPlatformException.WorkflowExecutionException.class,
                () -> service.executeWorkflow(workflowName, parameters)
        );

        assertTrue(exception.getMessage().contains("Unexpected response status"));
    }

    @Test
    void executeWorkflowAsync_Success() throws ExecutionException, InterruptedException {
        // Arrange
        String workflowName = "test-workflow";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("env", "prod");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", "67890");
        responseBody.put("status", "running");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        CompletableFuture<AutomationExecutionResult> futureResult = 
                service.executeWorkflowAsync(workflowName, parameters);

        // Assert
        assertNotNull(futureResult);
        AutomationExecutionResult result = futureResult.get();
        assertNotNull(result);
        assertEquals("67890", result.getJobId());
        assertEquals("running", result.getStatus());
        assertTrue(result.isSuccessful());
    }

    @Test
    void executeWorkflowAsync_Failure() throws ExecutionException, InterruptedException {
        // Arrange
        String workflowName = "test-workflow";
        Map<String, Object> parameters = new HashMap<>();

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("Network error"));

        // Act
        CompletableFuture<AutomationExecutionResult> futureResult = 
                service.executeWorkflowAsync(workflowName, parameters);

        // Assert
        assertNotNull(futureResult);
        AutomationExecutionResult result = futureResult.get();
        assertNotNull(result);
        assertFalse(result.isSuccessful());
        assertTrue(result.getMessage().contains("Async execution failed"));
        assertEquals("WORKFLOW_EXECUTION_FAILED", result.getErrorDetails());
    }

    @Test
    void getJobStatus_Success() throws AutomationPlatformException {
        // Arrange
        String jobId = "12345";
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", jobId);
        responseBody.put("status", "successful");
        responseBody.put("result_traceback", "Job completed successfully");
        responseBody.put("elapsed", 125.5);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        AutomationJobStatus status = service.getJobStatus(jobId);

        // Assert
        assertNotNull(status);
        assertEquals(jobId, status.getJobId());
        assertEquals(AutomationJobStatus.Status.SUCCESSFUL, status.getStatus());
        assertEquals("Job completed successfully", status.getStatusMessage());
        assertNotNull(status.getResult());
        assertEquals(responseBody, status.getResult());

        verify(restTemplate).exchange(
                eq(BASE_URL + "/jobs/" + jobId + "/"),
                eq(HttpMethod.GET),
                httpEntityVoidCaptor.capture(),
                eq(Map.class)
        );
    }

    @Test
    void getJobStatus_PendingStatus() throws AutomationPlatformException {
        // Arrange
        String jobId = "12345";
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", jobId);
        responseBody.put("status", "pending");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        AutomationJobStatus status = service.getJobStatus(jobId);

        // Assert
        assertEquals(AutomationJobStatus.Status.PENDING, status.getStatus());
    }

    @Test
    void getJobStatus_RunningStatus() throws AutomationPlatformException {
        // Arrange
        String jobId = "12345";
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "running");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        AutomationJobStatus status = service.getJobStatus(jobId);

        // Assert
        assertEquals(AutomationJobStatus.Status.RUNNING, status.getStatus());
    }

    @Test
    void getJobStatus_FailedStatus() throws AutomationPlatformException {
        // Arrange
        String jobId = "12345";
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "failed");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        AutomationJobStatus status = service.getJobStatus(jobId);

        // Assert
        assertEquals(AutomationJobStatus.Status.FAILED, status.getStatus());
    }

    @Test
    void getJobStatus_CancelledStatus() throws AutomationPlatformException {
        // Arrange
        String jobId = "12345";
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "canceled");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        AutomationJobStatus status = service.getJobStatus(jobId);

        // Assert
        assertEquals(AutomationJobStatus.Status.CANCELLED, status.getStatus());
    }

    @Test
    void getJobStatus_CancelledAlternativeSpelling() throws AutomationPlatformException {
        // Arrange
        String jobId = "12345";
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "cancelled");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        AutomationJobStatus status = service.getJobStatus(jobId);

        // Assert
        assertEquals(AutomationJobStatus.Status.CANCELLED, status.getStatus());
    }

    @Test
    void getJobStatus_UnknownStatus() throws AutomationPlatformException {
        // Arrange
        String jobId = "12345";
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "unknown_status");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        AutomationJobStatus status = service.getJobStatus(jobId);

        // Assert
        assertEquals(AutomationJobStatus.Status.ERROR, status.getStatus());
    }

    @Test
    void getJobStatus_NullStatus() throws AutomationPlatformException {
        // Arrange
        String jobId = "12345";
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", null);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        AutomationJobStatus status = service.getJobStatus(jobId);

        // Assert
        assertEquals(AutomationJobStatus.Status.ERROR, status.getStatus());
    }

    @Test
    void getJobStatus_JobNotFound() {
        // Arrange
        String jobId = "99999";

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("404 Not Found"));

        // Act & Assert
        AutomationPlatformException.JobNotFoundException exception = assertThrows(
                AutomationPlatformException.JobNotFoundException.class,
                () -> service.getJobStatus(jobId)
        );

        assertTrue(exception.getMessage().contains(jobId));
        assertEquals("JOB_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void getJobStatus_NullResponseBody() {
        // Arrange
        String jobId = "12345";

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act & Assert
        assertThrows(
                AutomationPlatformException.JobNotFoundException.class,
                () -> service.getJobStatus(jobId)
        );
    }

    @Test
    void getWorkflowJobStatus_Success() throws AutomationPlatformException {
        // Arrange
        String workflowId = "67890";
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", workflowId);
        responseBody.put("status", "successful");
        responseBody.put("result_traceback", "Workflow completed");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        AutomationJobStatus status = service.getWorkflowJobStatus(workflowId);

        // Assert
        assertNotNull(status);
        assertEquals(workflowId, status.getJobId());
        assertEquals(AutomationJobStatus.Status.SUCCESSFUL, status.getStatus());
        assertEquals("Workflow completed", status.getStatusMessage());

        verify(restTemplate).exchange(
                eq(BASE_URL + "/workflow_jobs/" + workflowId + "/"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        );
    }

    @Test
    void getWorkflowJobStatus_NotFound() {
        // Arrange
        String workflowId = "99999";

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("404 Not Found"));

        // Act & Assert
        AutomationPlatformException.JobNotFoundException exception = assertThrows(
                AutomationPlatformException.JobNotFoundException.class,
                () -> service.getWorkflowJobStatus(workflowId)
        );

        assertTrue(exception.getMessage().contains(workflowId));
        assertEquals("JOB_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void validateConnection_Success() {
        // Arrange
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("ping", "pong");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        boolean isValid = service.validateConnection();

        // Assert
        assertTrue(isValid);
        verify(restTemplate).exchange(
                eq(BASE_URL + "/ping/"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        );
    }

    @Test
    void validateConnection_Failure() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("Connection refused"));

        // Act
        boolean isValid = service.validateConnection();

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateConnection_UnsuccessfulStatusCode() {
        // Arrange
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        boolean isValid = service.validateConnection();

        // Assert
        assertFalse(isValid);
    }

    @Test
    void isHealthy_Success() {
        // Arrange
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("ping", "pong");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        boolean isHealthy = service.isHealthy();

        // Assert
        assertTrue(isHealthy);
    }

    @Test
    void isHealthy_Failure() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("Service unavailable"));

        // Act
        boolean isHealthy = service.isHealthy();

        // Assert
        assertFalse(isHealthy);
    }

    @Test
    void executeWorkflow_VerifyAuthHeaders() throws AutomationPlatformException {
        // Arrange
        String workflowName = "test-workflow";
        Map<String, Object> parameters = new HashMap<>();

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", "12345");
        responseBody.put("status", "pending");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        service.executeWorkflow(workflowName, parameters);

        // Assert
        verify(restTemplate).postForEntity(anyString(), httpEntityCaptor.capture(), eq(Map.class));
        HttpHeaders headers = httpEntityCaptor.getValue().getHeaders();

        // Verify Basic Auth is set
        assertNotNull(headers.getFirst(HttpHeaders.AUTHORIZATION));
        assertTrue(headers.getFirst(HttpHeaders.AUTHORIZATION).startsWith("Basic "));

        // Verify Content-Type
        assertEquals("application/json", headers.getFirst("Content-Type"));
    }

    @Test
    void getJobStatus_VerifyUrl() throws AutomationPlatformException {
        // Arrange
        String jobId = "test-job-123";
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", jobId);
        responseBody.put("status", "running");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        service.getJobStatus(jobId);

        // Assert
        verify(restTemplate).exchange(
                eq(BASE_URL + "/jobs/" + jobId + "/"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        );
    }

    @Test
    void getWorkflowJobStatus_VerifyUrl() throws AutomationPlatformException {
        // Arrange
        String workflowId = "test-workflow-456";
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", workflowId);
        responseBody.put("status", "pending");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        service.getWorkflowJobStatus(workflowId);

        // Assert
        verify(restTemplate).exchange(
                eq(BASE_URL + "/workflow_jobs/" + workflowId + "/"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        );
    }

    @Test
    void executeWorkflow_VerifyExtraVarsPassedCorrectly() throws AutomationPlatformException {
        // Arrange
        String workflowName = "deploy-app";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("app_name", "my-app");
        parameters.put("version", "1.2.3");
        parameters.put("replicas", 3);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", "99999");
        responseBody.put("status", "pending");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        service.executeWorkflow(workflowName, parameters);

        // Assert
        verify(restTemplate).postForEntity(anyString(), httpEntityCaptor.capture(), eq(Map.class));
        Map<String, Object> requestBody = httpEntityCaptor.getValue().getBody();
        assertNotNull(requestBody);
        
        Map<String, Object> extraVars = (Map<String, Object>) requestBody.get("extra_vars");
        assertNotNull(extraVars);
        assertEquals("my-app", extraVars.get("app_name"));
        assertEquals("1.2.3", extraVars.get("version"));
        assertEquals(3, extraVars.get("replicas"));
    }

    @Test
    void executeWorkflow_EmptyParameters() throws AutomationPlatformException {
        // Arrange
        String workflowName = "test-workflow";
        Map<String, Object> parameters = new HashMap<>();

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", "12345");
        responseBody.put("status", "pending");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        AutomationExecutionResult result = service.executeWorkflow(workflowName, parameters);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccessful());
        verify(restTemplate).postForEntity(anyString(), httpEntityCaptor.capture(), eq(Map.class));
    }
}
