package org.opendevstack.apiservice.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityPropertiesTest {

    @Test
    void defaultPublicEndpoints_doNotExposeH2Console() {
        SecurityProperties properties = new SecurityProperties();
        String[] publicEndpoints = (String[]) ReflectionTestUtils.getField(properties, "publicEndpoints");

        assertFalse(Arrays.asList(publicEndpoints).contains("/h2-console/**"));
    }

    @Test
    void defaultPublicEndpoints_keepMinimalSafeEndpoints() {
        SecurityProperties properties = new SecurityProperties();
        String[] publicEndpoints = (String[]) ReflectionTestUtils.getField(properties, "publicEndpoints");

        assertTrue(Arrays.asList(publicEndpoints).contains("/api/public/**"));
        assertTrue(Arrays.asList(publicEndpoints).contains("/actuator/health"));
        assertTrue(Arrays.asList(publicEndpoints).contains("/actuator/info"));
    }
}
