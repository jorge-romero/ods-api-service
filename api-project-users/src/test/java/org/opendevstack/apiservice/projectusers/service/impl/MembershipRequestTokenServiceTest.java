package org.opendevstack.apiservice.projectusers.service.impl;

import org.opendevstack.apiservice.projectusers.exception.InvalidTokenException;
import org.opendevstack.apiservice.projectusers.exception.ProjectUserException;
import org.opendevstack.apiservice.projectusers.service.JwtMembershipRequestClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MembershipRequestTokenServiceTest {

    private MembershipRequestTokenServiceImpl tokenService;

    @BeforeEach
    void setUp() {
        // Use a 256-bit (32 character) secret key for JWT security requirements
        tokenService = new MembershipRequestTokenServiceImpl("test-secret-key-for-jwt-testing-32chars-minimum", 24);
    }

    @Test
    void testCreateAndDecodeToken() throws ProjectUserException {
        // Arrange
        String jobId = "12345";
        String projectKey = "my-project";
        String user = "john.doe";
        String environment = "DEVELOPMENT";
        String role = "TEAM";
        LocalDateTime initiatedAt = LocalDateTime.now();
        String initiatedBy = "admin.user";

        // Act
        String requestId = tokenService.createRequestToken(jobId, null, projectKey, user, environment, role, initiatedAt,
                initiatedBy);
        Map<String, Object> decodedData = tokenService.decodeRequestToken(requestId);

        // Assert
        assertNotNull(requestId);
        assertTrue(requestId.startsWith("req_"));
        assertEquals(jobId, decodedData.get(JwtMembershipRequestClaims.CLAIM_JOB_ID));
        assertEquals(projectKey, decodedData.get(JwtMembershipRequestClaims.CLAIM_PROJECT_KEY));
        assertEquals(user, decodedData.get(JwtMembershipRequestClaims.CLAIM_USER));
        assertEquals(environment, decodedData.get(JwtMembershipRequestClaims.CLAIM_ENVIRONMENT));
        assertEquals(role, decodedData.get(JwtMembershipRequestClaims.CLAIM_ROLE));
        assertEquals(initiatedBy, decodedData.get(JwtMembershipRequestClaims.CLAIM_INITIATED_BY));
        assertNotNull(decodedData.get(JwtMembershipRequestClaims.CLAIM_INITIATED_AT));
    }

    @Test
    void testIsValidToken() throws ProjectUserException {
        // Arrange
        String requestId = tokenService.createRequestToken("123", null, "proj", "user", "dev", "TEAM", LocalDateTime.now(),
                "admin");

        // Act & Assert
        assertTrue(tokenService.isValidToken(requestId));
        assertFalse(tokenService.isValidToken("invalid-token"));
    }

    @Test
    void testExtractJobId() throws ProjectUserException {
        // Arrange
        String jobId = "98765";
        String requestId = tokenService.createRequestToken(jobId, null, "proj", "user", "dev", "TEAM", LocalDateTime.now(),
                "admin");

        // Act
        String extractedJobId = tokenService.extractJobId(requestId);

        // Assert
        assertEquals(jobId, extractedJobId);
    }

    @Test
    void testInvalidTokenFormat() {
        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> {
            tokenService.decodeRequestToken("invalid-format");
        });

        assertThrows(InvalidTokenException.class, () -> {
            tokenService.decodeRequestToken("req_without_second_underscore");
        });
    }
}