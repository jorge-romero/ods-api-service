package org.opendevstack.apiservice.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.persistence.entity.ClientAppEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests for {@link ClientAppRepository}.
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ClientAppRepositoryTest {

	@Container
	@SuppressWarnings("resource")
	static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine")
		.withDatabaseName("devstack_test")
		.withUsername("test")
		.withPassword("test");

	@DynamicPropertySource
	static void postgresProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.datasource.hikari.maximum-pool-size", () -> "2");
		registry.add("spring.datasource.hikari.minimum-idle", () -> "0");
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
	}

	@Autowired
	private ClientAppRepository repository;

	private ClientAppEntity enabledClientApp;

	@BeforeEach
	void setUp() {
		repository.deleteAll();

		enabledClientApp = createClientApp("11111111-1111-1111-1111-111111111111", true);
		createClientApp("22222222-2222-2222-2222-222222222222", false);
	}

	private ClientAppEntity createClientApp(String clientId, boolean enabled) {
		ClientAppEntity clientApp = ClientAppEntity.builder()
			.clientId(clientId)
			.clientName(enabled ? "Enabled app" : "Disabled app")
			.enabled(enabled)
			.build();

		return repository.saveAndFlush(clientApp);
	}

	@Nested
	@DisplayName("findByClientId")
	class FindByClientId {

		@Test
		@DisplayName("returns the matching client app")
		void returnsMatchingClientApp() {
			Optional<ClientAppEntity> found = repository.findByClientId("11111111-1111-1111-1111-111111111111");

			assertThat(found).isPresent();
			assertThat(found.get().getClientName()).isEqualTo("Enabled app");
			assertThat(found.get().isEnabled()).isTrue();
		}

		@Test
		@DisplayName("returns empty for unknown client id")
		void returnsEmptyForUnknownClientId() {
			assertThat(repository.findByClientId("99999999-9999-9999-9999-999999999999")).isEmpty();
		}

	}

	@Nested
	@DisplayName("findByEnabledTrue")
	class FindByEnabledTrue {

		@Test
		@DisplayName("returns only enabled client apps")
		void returnsOnlyEnabledClientApps() {
			List<ClientAppEntity> enabledApps = repository.findByEnabledTrue();

			assertThat(enabledApps).hasSize(1);
			assertThat(enabledApps.get(0).getClientId()).isEqualTo("11111111-1111-1111-1111-111111111111");
		}

	}
	@Nested
	@DisplayName("existsByClientId")
	class ExistsByClientId {

		@Test
		@DisplayName("returns true for existing client id")
		void returnsTrueForExistingClientId() {
			assertThat(repository.existsByClientId("11111111-1111-1111-1111-111111111111")).isTrue();
		}

		@Test
		@DisplayName("returns false for unknown client id")
		void returnsFalseForUnknownClientId() {
			assertThat(repository.existsByClientId("99999999-9999-9999-9999-999999999999")).isFalse();
		}

	}

}