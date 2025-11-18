package org.opendevstack.apiservice.externalservice.bitbucket.integration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Test configuration for Bitbucket integration tests.
 * This configuration enables Spring Boot auto-configuration and component scanning
 * for the bitbucket service module.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "org.opendevstack.apiservice.externalservice.bitbucket")
public class BitbucketIntegrationTestConfig {
    // Configuration class for integration tests
    // All beans will be auto-configured by Spring Boot
}
