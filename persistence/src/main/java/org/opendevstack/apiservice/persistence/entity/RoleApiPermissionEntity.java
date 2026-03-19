package org.opendevstack.apiservice.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * JPA entity mapping the {@code role_api_permissions} table.
 */
@Entity
@Table(name = "role_api_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"role", "apiResource"})
public class RoleApiPermissionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "role_id", nullable = false)
	private RoleEntity role;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "api_resource_id", nullable = false)
	private ApiResourceEntity apiResource;

	/** {@code true} = allow, {@code false} = explicit deny (deny wins). */
	@Column(name = "allowed", nullable = false)
	@Builder.Default
	private boolean allowed = true;

	@Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime createdAt;

	@PrePersist
	void onPrePersist() {
		this.createdAt = OffsetDateTime.now();
	}

}
