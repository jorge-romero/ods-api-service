package org.opendevstack.apiservice.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.opendevstack.apiservice.persistence.entity.ClientAppEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link ClientAppEntity}.
 */
@Repository
public interface ClientAppRepository extends JpaRepository<ClientAppEntity, UUID> {

	/**
	 * Finds a client application by its Azure AD client identifier.
	 * @param clientId Azure AD application/client UUID
	 * @return matching client app, if present
	 */
	Optional<ClientAppEntity> findByClientId(UUID clientId);

	/**
	 * Returns all enabled client applications.
	 * @return enabled client apps
	 */
	List<ClientAppEntity> findByEnabledTrue();

	/**
	 * Checks if a client application exists for the given Azure AD client identifier.
	 * @param clientId Azure AD application/client UUID
	 * @return {@code true} if the client app exists
	 */
	boolean existsByClientId(UUID clientId);

	/**
	 * Loads a client application together with its project flavor configuration.
	 * @param clientId Azure AD application/client UUID
	 * @return client app with project flavors initialized, if present
	 */
	@EntityGraph(attributePaths = "projectFlavors")
	Optional<ClientAppEntity> findDetailedByClientId(UUID clientId);

}