package org.opendevstack.apiservice.persistence.repository;

import java.util.List;
import java.util.UUID;
import org.opendevstack.apiservice.persistence.entity.RoleApiPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleApiPermissionRepository extends JpaRepository<RoleApiPermissionEntity, UUID> {

	/**
	 * Returns all API permissions (allow/deny) for the given role names.
	 */
	@Query("""
			SELECT rap FROM RoleApiPermissionEntity rap
			JOIN FETCH rap.apiResource ar
			JOIN rap.role r
			WHERE r.name IN :roleNames
			""")
	List<RoleApiPermissionEntity> findByRoleNames(@Param("roleNames") List<String> roleNames);

	/**
	 * Returns all API permissions for a given Azure AD client identifier,
	 * joining through client_app_roles → roles → role_api_permissions.
	 */
	@Query("""
			SELECT rap FROM RoleApiPermissionEntity rap
			JOIN FETCH rap.apiResource ar
			JOIN rap.role r
			JOIN ClientAppRoleEntity car ON car.role = r
			JOIN car.clientApp ca
			WHERE ca.clientId = :clientId AND ca.enabled = true
			""")
	List<RoleApiPermissionEntity> findByClientId(@Param("clientId") String clientId);

}
