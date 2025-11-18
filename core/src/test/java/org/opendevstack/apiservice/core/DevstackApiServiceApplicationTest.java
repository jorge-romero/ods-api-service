package org.opendevstack.apiservice.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DevstackApiServiceApplicationTest {

    @Test
    void contextLoads() {
        // This test ensures that the Spring context loads successfully
    }

    @Test
    void applicationStartsSuccessfully() {
        // This test verifies that the application class can be instantiated
        DevstackApiServiceApplication app = new DevstackApiServiceApplication();
        org.junit.jupiter.api.Assertions.assertNotNull(app, "Application instance should not be null");
    }
}