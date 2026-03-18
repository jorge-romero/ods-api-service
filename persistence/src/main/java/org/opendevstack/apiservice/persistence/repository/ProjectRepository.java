package org.opendevstack.apiservice.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.opendevstack.apiservice.persistence.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link ProjectEntity}.
 *
 * <p>
 * Provides standard CRUD operations inherited from {@link JpaRepository} plus
 * domain-specific derived query methods. All query methods that return project
 * collections exclude soft-deleted records (i.e. {@code deleted = false}) by
 * convention — use {@code findAllIncludingDeleted()} variants explicitly when
 * deleted records are needed.
 * </p>
 */
@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {
	
	Optional<ProjectEntity> findByProjectKeyIgnoreCase(String projectKey);
	
	List<ProjectEntity> findByDeletedFalse();
	
	boolean existsByProjectKeyIgnoreCase(String projectKey);

}
