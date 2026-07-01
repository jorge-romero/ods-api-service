package org.opendevstack.apiservice.persistence;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Shared Spring Boot test bootstrap for the persistence module.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
public class PersistenceTestApplication {

}