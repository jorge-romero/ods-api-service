package org.opendevstack.apiservice.persistence.integration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Test configuration for persistence integration tests.
 * Enables Spring Boot auto-configuration, entity scanning, and JPA repository
 * scanning for the persistence module.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EntityScan(basePackages = "org.opendevstack.apiservice.persistence.entity")
@EnableJpaRepositories(basePackages = "org.opendevstack.apiservice.persistence.repository")
public class PersistenceIntegrationTestConfig {

}
