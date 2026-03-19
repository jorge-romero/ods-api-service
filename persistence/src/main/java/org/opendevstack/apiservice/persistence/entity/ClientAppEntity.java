package org.opendevstack.apiservice.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * JPA entity mapping the {@code client_apps} table.
 */
@Entity
@Table(name = "client_apps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"projectFlavors", "appRoles"})
public class ClientAppEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	/** Azure AD application/client identifier. */
	@Column(name = "client_id", nullable = false, unique = true, length = 36)
	private String clientId;

	/** Optional display name for the client application. */
	@Column(name = "client_name", length = 255)
	private String clientName;

	/** Whether the client is allowed to call the API. */
	@Column(name = "enabled", nullable = false)
	@Builder.Default
	private boolean enabled = true;

	/** Project creation flavors configured for this client app. */
	@OneToMany(mappedBy = "clientApp", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<ClientAppProjectFlavorEntity> projectFlavors = new LinkedHashSet<>();

	/** Roles assigned to this client application. */
	@OneToMany(mappedBy = "clientApp", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<ClientAppRoleEntity> appRoles = new LinkedHashSet<>();

	/** Original creation timestamp (UTC). Set automatically on first persist. */
	@Column(name = "created_at", nullable = false, updatable = false,
			columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime createdAt;

	/** Timestamp of last update (UTC). Updated automatically on every merge. */
	@Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime updatedAt;

	/**
	 * Adds a project flavor and synchronizes the reverse side of the relationship.
	 * @param projectFlavor flavor to attach
	 */
	public void addProjectFlavor(ClientAppProjectFlavorEntity projectFlavor) {
		projectFlavors.add(projectFlavor);
		projectFlavor.setClientApp(this);
	}

	/**
	 * Removes a project flavor and synchronizes the reverse side of the relationship.
	 * @param projectFlavor flavor to detach
	 */
	public void removeProjectFlavor(ClientAppProjectFlavorEntity projectFlavor) {
		projectFlavors.remove(projectFlavor);
		projectFlavor.setClientApp(null);
	}

	@PrePersist
	void onPrePersist() {
		OffsetDateTime now = OffsetDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onPreUpdate() {
		this.updatedAt = OffsetDateTime.now();
	}

}