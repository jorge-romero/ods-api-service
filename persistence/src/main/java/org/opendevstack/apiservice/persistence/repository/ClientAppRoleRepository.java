package org.opendevstack.apiservice.persistence.repository;

import java.util.List;
import java.util.UUID;
import org.opendevstack.apiservice.persistence.entity.ClientAppRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientAppRoleRepository extends JpaRepository<ClientAppRoleEntity, UUID> {

	/**
	 * Returns the role names assigned to the given Azure AD client identifier.
	 */
	@Query("""
			SELECT r.name FROM ClientAppRoleEntity car
			JOIN car.role r
			JOIN car.clientApp ca
			WHERE ca.clientId = :clientId AND ca.enabled = true
			""")
	List<String> findRoleNamesByClientId(@Param("clientId") String clientId);

}
