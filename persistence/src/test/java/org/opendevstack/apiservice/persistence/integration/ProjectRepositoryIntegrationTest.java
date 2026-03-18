package org.opendevstack.apiservice.persistence.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.opendevstack.apiservice.persistence.entity.ProjectEntity;
import org.opendevstack.apiservice.persistence.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import lombok.extern.slf4j.Slf4j;

/**
 * Full-context integration tests for {@link ProjectRepository} against a real
 * local PostgreSQL database.

 * <p>
 * Requires a running PostgreSQL instance reachable via the environment
 * variables consumed by these tests. If no environment variables are provided,
 * the defaults point to the local development database.
 *
 * <p>
 * Test data is inserted under well-known keys ({@code IT-ACTIVE-01},
 * {@code IT-DELETED-01},
 * {@code IT-PENDING-01}) and cleaned up in {@code @AfterEach}, so the tests do
 * not interfere with existing data in the database.
 * </p>
 */
@SpringBootTest(classes = PersistenceIntegrationTestConfig.class)
@ActiveProfiles("local")
@TestPropertySource(properties = { "spring.config.activate.on-profile=local" })
@EnabledIfEnvironmentVariable(named = "PROJECT_REPOSITORY_INTEGRATION_TEST_ENABLED", matches = "true")
@Slf4j
class ProjectRepositoryIntegrationTest {

    private static final String DEFAULT_TEST_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/ods_api_service";
    private static final String DEFAULT_TEST_DATASOURCE_USERNAME = "ods_api_service";
    private static final String DEFAULT_TEST_DATASOURCE_PASSWORD = "ods_api_service";

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url",
        () -> System.getenv().getOrDefault("IT_SPRING_DATASOURCE_URL", DEFAULT_TEST_DATASOURCE_URL));
    registry.add("spring.datasource.username",
        () -> System.getenv().getOrDefault("IT_SPRING_DATASOURCE_USERNAME", DEFAULT_TEST_DATASOURCE_USERNAME));
    registry.add("spring.datasource.password",
        () -> System.getenv().getOrDefault("IT_SPRING_DATASOURCE_PASSWORD", DEFAULT_TEST_DATASOURCE_PASSWORD));
    }

    /** Keys reserved exclusively for these integration tests. */
    private static final Set<String> TEST_KEYS = Set.of("IT-ACTIVE-01", "IT-DELETED-01", "IT-PENDING-01",
            "IT-DUPLICATE-01");

    @Autowired
    private ProjectRepository projectRepository;

    private ProjectEntity activeProject;
    private ProjectEntity deletedProject;

    @BeforeEach
    void setUp() {
        cleanupTestData();

        activeProject = projectRepository.save(ProjectEntity.builder()
                .projectKey("IT-ACTIVE-01")
                .projectName("IT Active Project")
                .configurationItem("CI-IT-001")
                .location("eu")
                .projectFlavor("AMP")
                .deleted(false)
                .build());

        deletedProject = projectRepository.save(ProjectEntity.builder()
                .projectKey("IT-DELETED-01")
                .projectName("IT Deleted Project")
                .configurationItem("CI-IT-002")
                .location("us")
                .projectFlavor("DLSS")
                .status("Failed")
                .deleted(true)
                .build());

        // Pending project — active but not yet provisioned
        projectRepository.save(ProjectEntity.builder()
                .projectKey("IT-PENDING-01")
                .projectName("IT Pending Project")
                .configurationItem("CI-IT-003")
                .location("eu")
                .projectFlavor("AMP")
                .status("Pending")
                .deleted(false)
                .build());
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    private void cleanupTestData() {
        TEST_KEYS.forEach(key -> projectRepository.findByProjectKeyIgnoreCase(key).ifPresent(projectRepository::delete));
    }

    @Nested
    @DisplayName("findByProjectKeyIgnoreCase")
    class findByProjectKeyIgnoreCase {

        @Test
        @DisplayName("returns active project by its key")
        void returnsActiveProjectByKey() {
            Optional<ProjectEntity> result = projectRepository.findByProjectKeyIgnoreCase("IT-ACTIVE-01");

            assertThat(result).isPresent();
            assertThat(result.get().getProjectKey()).isEqualTo("IT-ACTIVE-01");
            assertThat(result.get().isDeleted()).isFalse();
        }

        @Test
        @DisplayName("returns soft-deleted project by its key")
        void returnsDeletedProjectByKey() {
            Optional<ProjectEntity> result = projectRepository.findByProjectKeyIgnoreCase("IT-DELETED-01");

            assertThat(result).isPresent();
            assertThat(result.get().getProjectKey()).isEqualTo("IT-DELETED-01");
            assertThat(result.get().isDeleted()).isTrue();
        }

        @Test
        @DisplayName("returns empty Optional for a key that has never existed")
        void returnsEmptyForUnknownKey() {
            Optional<ProjectEntity> result = projectRepository.findByProjectKeyIgnoreCase("NONEXISTENTKEY");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returned entity contains all persisted fields")
        void returnedEntityContainsAllFields() {
            Optional<ProjectEntity> result = projectRepository.findByProjectKeyIgnoreCase("IT-DELETED-01");

            assertThat(result).isPresent();
            ProjectEntity entity = result.get();
            assertThat(entity.getProjectName()).isEqualTo("IT Deleted Project");
            assertThat(entity.getConfigurationItem()).isEqualTo("CI-IT-002");
            assertThat(entity.getLocation()).isEqualTo("us");
            assertThat(entity.getStatus()).isEqualTo("Failed");
        }
    }

    @Nested
    @DisplayName("findByDeletedFalse")
    class FindByDeletedFalse {

        @Test
        @DisplayName("returns only non-deleted projects")
        void returnsOnlyNonDeletedProjects() {
            List<ProjectEntity> active = projectRepository.findByDeletedFalse();

            assertThat(active)
                    .isNotEmpty()
                    .allMatch(p -> !p.isDeleted());
        }

        @Test
        @DisplayName("excludes soft-deleted projects from results")
        void excludesSoftDeletedProjects() {
            List<ProjectEntity> active = projectRepository.findByDeletedFalse();

            assertThat(active)
                    .isNotEmpty()
                    .extracting(ProjectEntity::getProjectKey)
                    .doesNotContain("IT-DELETED-01");
        }

        @Test
        @DisplayName("includes test active projects regardless of their status field")
        void includesAllActiveProjectsRegardlessOfStatus() {
            List<ProjectEntity> active = projectRepository.findByDeletedFalse();

            // The real DB may contain other projects; assert our test keys are present
            assertThat(active)
                    .extracting(ProjectEntity::getProjectKey)
                    .contains("IT-ACTIVE-01", "IT-PENDING-01");
        }
    }

    // ── existsByProjectKey ────────────────────────────────────────────────────

    @Nested
    @DisplayName("existsByProjectKey")
    class ExistsByProjectKey {

        @Test
        @DisplayName("returns true for an existing active project key")
        void returnsTrueForActiveKey() {
            assertThat(projectRepository.existsByProjectKeyIgnoreCase("IT-ACTIVE-01")).isTrue();
        }

        @Test
        @DisplayName("returns true for a soft-deleted project key")
        void returnsTrueForDeletedKey() {
            assertThat(projectRepository.existsByProjectKeyIgnoreCase("IT-DELETED-01")).isTrue();
        }

        @Test
        @DisplayName("returns false for a key that has never existed")
        void returnsFalseForNonExistentKey() {
            assertThat(projectRepository.existsByProjectKeyIgnoreCase("NONEXISTENTKEY")).isFalse();
        }
    }
    
    @Nested
    @DisplayName("Persistence lifecycle")
    class PersistenceLifecycle {

        @Test
        @DisplayName("save() auto-populates createdAt and updatedAt timestamps")
        void saveAutoPopulatesTimestamps() {
            assertThat(activeProject.getCreatedAt()).isNotNull();
            assertThat(activeProject.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("save() with duplicate project_key throws DataIntegrityViolationException")
        void saveDuplicateProjectKeyThrowsException() {
            ProjectEntity duplicate = ProjectEntity.builder()
                    .projectKey("IT-ACTIVE-01") // already exists
                    .configurationItem("CI-IT-999")
                    .location("eu")
                    .build();

            assertThatThrownBy(() -> projectRepository.saveAndFlush(duplicate))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("soft-delete by setting deleted=true is reflected in findByDeletedFalse")
        void softDeleteIsReflectedInActiveList() {
            activeProject.setDeleted(true);
            projectRepository.save(activeProject);

            assertThat(projectRepository.findByDeletedFalse())
                    .extracting(ProjectEntity::getProjectKey)
                    .doesNotContain("IT-ACTIVE-01");
        }

        @Test
        @DisplayName("restoring a soft-deleted project makes it reappear in findByDeletedFalse")
        void restoredProjectReappearsInActiveList() {
            deletedProject.setDeleted(false);
            projectRepository.save(deletedProject);

            assertThat(projectRepository.findByDeletedFalse())
                    .extracting(ProjectEntity::getProjectKey)
                    .contains("IT-DELETED-01");
        }
    }
}