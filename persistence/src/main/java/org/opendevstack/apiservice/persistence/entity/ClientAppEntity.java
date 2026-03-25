package org.opendevstack.apiservice.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
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


	/** Original creation timestamp (UTC). Set automatically on first persist. */
	@Column(name = "created_at", nullable = false, updatable = false,
			columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime createdAt;

	/** Timestamp of last update (UTC). Updated automatically on every merge. */
	@Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime updatedAt;

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