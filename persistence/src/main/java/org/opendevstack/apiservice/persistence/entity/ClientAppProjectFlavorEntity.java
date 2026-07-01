package org.opendevstack.apiservice.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * JPA entity mapping the {@code client_app_project_flavors} table.
 */
@Entity
@Table(name = "client_app_project_flavors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "clientApp")
public class ClientAppProjectFlavorEntity extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	/** Owning client application. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "client_app_id", nullable = false)
	private ClientAppEntity clientApp;

	/** Human-readable flavor name. */
	@Column(name = "name", nullable = false, length = 50)
	private String name;

	/** Project key generation pattern. */
	@Column(name = "project_key_pattern", nullable = false, length = 100)
	private String projectKeyPattern;

	/** Default project owner. */
	@Column(name = "project_owner", length = 255)
	private String projectOwner;

	/** Service account associated with the flavor. */
	@Column(name = "service_account", length = 255)
	private String serviceAccount;

	/** Default CMDB configuration item. */
	@Column(name = "config_item", length = 255)
	private String configItem;

	/** Allowed CMDB configuration item overrides. */
	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "allowed_config_items", nullable = false, columnDefinition = "TEXT[]")
	@Builder.Default
	private String[] allowedConfigItems = new String[0];

	/** Deployment region. */
	@Column(name = "location", length = 50)
	private String location;

}