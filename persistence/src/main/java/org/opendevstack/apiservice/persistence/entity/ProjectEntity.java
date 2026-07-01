package org.opendevstack.apiservice.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * JPA entity mapping the {@code projects} table.
 *
 * <p>
 * Schema is managed externally via Liquibase (see {@code database} module). Hibernate is
 * configured with {@code ddl-auto=validate} so it only validates alignment at boot — it never
 * modifies the schema.
 * </p>
 *
 * <p>
 * Soft-deletes are supported via the {@link #deleted} flag. Use
 * {@code findByDeletedFalse()} to retrieve only active projects.
 * </p>
 */
@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = { "description", "ldapGroupManager", "ldapGroupTeam", "ldapGroupStakeholder" })
public class ProjectEntity extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	/**
	 * Human-readable unique identifier (e.g. {@code MY-PROJECT}). Maps to the
	 * {@code uq_projects_project_key} unique index.
	 */
	@Column(name = "project_key", nullable = false, unique = true, length = 100)
	private String projectKey;

	/** Optional display name. */
	@Column(name = "project_name", length = 255)
	private String projectName;

	/** Optional free-text description. */
	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	/** Configuration item identifier. */
	@Column(name = "configuration_item", nullable = false, length = 255)
	private String configurationItem;

	/**
	 * Deployment region.
	 */
	@Column(name = "location", nullable = false, length = 50)
	private String location;

	/**
	 * Project type classifier.
	 */
	@Column(name = "project_flavor", length = 50)
	private String projectFlavor;

	/**
	 * Provisioning status. Known values: {@code Pending}, {@code Running}, {@code Failed}
	 * ({@code null} = completed successfully).
	 */
	@Column(name = "status", length = 50)
	private String status;

	/**
	 * Soft-delete flag. Active projects have {@code deleted = false}.
	 */
	@Column(name = "deleted", nullable = false)
	@Builder.Default
	private boolean deleted = false;

	/** Full LDAP DN for the PROJECT_MANAGER group. */
	@Column(name = "ldap_group_manager", columnDefinition = "TEXT")
	private String ldapGroupManager;

	/** Full LDAP DN for the PROJECT_TEAM group. */
	@Column(name = "ldap_group_team", columnDefinition = "TEXT")
	private String ldapGroupTeam;

	/** Full LDAP DN for the PROJECT_STAKEHOLDER group. */
	@Column(name = "ldap_group_stakeholder", columnDefinition = "TEXT")
	private String ldapGroupStakeholder;

}
