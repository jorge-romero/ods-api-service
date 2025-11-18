package org.opendevstack.apiservice.projectusers.service.impl;

import org.opendevstack.apiservice.projectusers.exception.InvalidTokenException;
import org.opendevstack.apiservice.projectusers.exception.TokenCreationException;
import org.opendevstack.apiservice.projectusers.exception.TokenDecodingException;
import org.opendevstack.apiservice.projectusers.exception.TokenExpiredException;
import org.opendevstack.apiservice.projectusers.service.MembershipRequestTokenService;
import org.opendevstack.apiservice.projectusers.service.JwtMembershipRequestClaims;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT-based implementation of MembershipRequestTokenService.
 * Provides stateless request tracking using signed JWT tokens.
 */
@Service("membershipRequestTokenService")
public class MembershipRequestTokenServiceImpl implements MembershipRequestTokenService {

    private static final Logger logger = LoggerFactory.getLogger(MembershipRequestTokenServiceImpl.class);

    private final SecretKey secretKey;
    private final long tokenExpirationHours;

    public MembershipRequestTokenServiceImpl(
            @Value("${apis.project-users.token.secret:devstack-api-service-jwt-secret-key-256bit-change-in-production}") String secret,
            @Value("${apis.project-users.token.expiration-hours:24}") long tokenExpirationHours) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.tokenExpirationHours = tokenExpirationHours;
    }

    @Override
    public String createRequestToken(String jobId, String uipathReference, String projectKey, String user,
            String environment, String role,
            LocalDateTime initiatedAt, String initiatedBy) {
        try {
            Date now = new Date();
            Date expiration = new Date(now.getTime() + (tokenExpirationHours * 60 * 60 * 1000));

            var builder = Jwts.builder()
                    .claim(JwtMembershipRequestClaims.CLAIM_JOB_ID, jobId)
                    .claim(JwtMembershipRequestClaims.CLAIM_PROJECT_KEY, projectKey)
                    .claim(JwtMembershipRequestClaims.CLAIM_USER, user)
                    .claim(JwtMembershipRequestClaims.CLAIM_ENVIRONMENT, environment)
                    .claim(JwtMembershipRequestClaims.CLAIM_ROLE, role)
                    .claim(JwtMembershipRequestClaims.CLAIM_INITIATED_AT, initiatedAt.toEpochSecond(ZoneOffset.UTC))
                    .claim(JwtMembershipRequestClaims.CLAIM_INITIATED_BY, initiatedBy)
                    .claim(JwtMembershipRequestClaims.CLAIM_UIPATH_REFERENCE, uipathReference);
            
            String token = builder
                    .subject("membership-request")
                    .issuedAt(now)
                    .expiration(expiration)
                    .signWith(secretKey)
                    .compact();

            // Generate user-friendly request ID with prefix and embed the full token
            String requestId = "req_" + System.currentTimeMillis() + "_" + token;

            logger.debug("Created request token for job '{}', project '{}', user '{}'", jobId, projectKey, user);
            return requestId;

        } catch (Exception e) {
            logger.warn("Failed to create request token for job '{}': {}", jobId, e.getMessage(), e);
            throw new TokenCreationException("Failed to create request token", e);
        }
    }

    @Override
    public Map<String, Object> decodeRequestToken(String token) {
        try {
            String jwtToken = extractJwtFromRequestId(token);

            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwtToken)
                    .getPayload();

            Map<String, Object> result = new HashMap<>();
            result.put(JwtMembershipRequestClaims.CLAIM_JOB_ID,
                    claims.get(JwtMembershipRequestClaims.CLAIM_JOB_ID, String.class));
            result.put(JwtMembershipRequestClaims.CLAIM_UIPATH_REFERENCE,
                    claims.get(JwtMembershipRequestClaims.CLAIM_UIPATH_REFERENCE, String.class));
            result.put(JwtMembershipRequestClaims.CLAIM_PROJECT_KEY,
                    claims.get(JwtMembershipRequestClaims.CLAIM_PROJECT_KEY, String.class));
            result.put(JwtMembershipRequestClaims.CLAIM_USER,
                    claims.get(JwtMembershipRequestClaims.CLAIM_USER, String.class));
            result.put(JwtMembershipRequestClaims.CLAIM_ENVIRONMENT,
                    claims.get(JwtMembershipRequestClaims.CLAIM_ENVIRONMENT, String.class));
            result.put(JwtMembershipRequestClaims.CLAIM_ROLE,
                    claims.get(JwtMembershipRequestClaims.CLAIM_ROLE, String.class));
            result.put(JwtMembershipRequestClaims.CLAIM_INITIATED_BY,
                    claims.get(JwtMembershipRequestClaims.CLAIM_INITIATED_BY, String.class));

            Long initiatedAtEpoch = claims.get(JwtMembershipRequestClaims.CLAIM_INITIATED_AT, Long.class);
            if (initiatedAtEpoch != null) {
                result.put(JwtMembershipRequestClaims.CLAIM_INITIATED_AT,
                        LocalDateTime.ofEpochSecond(initiatedAtEpoch, 0, ZoneOffset.UTC));
            }

            return result;

        } catch (ExpiredJwtException e) {
            logger.warn("Request token expired: {}", e.getMessage());
            throw new TokenExpiredException("Request token has expired");
        } catch (JwtException e) {
            logger.warn("Invalid request token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid request token");
        } catch (InvalidTokenException e) {
            // Re-throw InvalidTokenException as-is
            throw e;
        } catch (Exception e) {
            logger.warn("Failed to decode request token: {}", e.getMessage(), e);
            throw new TokenDecodingException("Failed to decode request token", e);
        }
    }

    @Override
    public boolean isValidToken(String token) {
        try {
            String jwtToken = extractJwtFromRequestId(token);
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwtToken);
            return true;
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String extractJobId(String token) {
        try {
            String jwtToken = extractJwtFromRequestId(token);
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwtToken)
                    .getPayload();
            return claims.get(JwtMembershipRequestClaims.CLAIM_JOB_ID, String.class);
        } catch (Exception e) {
            logger.error("Failed to extract job ID from token: {}", e.getMessage(), e);
            return null;
        }
    }

    private String extractJwtFromRequestId(String requestId) {
        if (requestId == null || !requestId.startsWith("req_")) {
            throw new InvalidTokenException("Invalid request ID format");
        }

        // Find the second underscore to extract the JWT token
        int firstUnderscore = requestId.indexOf('_');
        int secondUnderscore = requestId.indexOf('_', firstUnderscore + 1);

        if (secondUnderscore == -1) {
            throw new InvalidTokenException("Invalid request ID format");
        }

        return requestId.substring(secondUnderscore + 1);
    }
}