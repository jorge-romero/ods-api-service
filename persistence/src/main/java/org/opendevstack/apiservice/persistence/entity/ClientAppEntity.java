package org.opendevstack.apiservice.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
public class ClientAppEntity extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	/** Azure AD application/client identifier. */
	@Column(name = "client_id", nullable = false, unique = true)
	private UUID clientId;

	/** Optional display name for the client application. */
	@Column(name = "client_name", length = 255)
	private String clientName;

	/** Whether the client is allowed to call the API. */
	@Column(name = "enabled", nullable = false)
	@Builder.Default
	private boolean enabled = true;

	/** Project flavor configurations associated with this client. */
	@OneToMany(mappedBy = "clientApp", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<ClientAppProjectFlavorEntity> projectFlavors = new ArrayList<>();

}