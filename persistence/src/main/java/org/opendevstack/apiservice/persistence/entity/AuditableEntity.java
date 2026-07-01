package org.opendevstack.apiservice.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Shared audit timestamps for persistence entities.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class AuditableEntity {

	/** Original creation timestamp (UTC). Set automatically on first persist. */
	@Column(name = "created_at", nullable = false, updatable = false,
			columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime createdAt;

	/** Timestamp of last update (UTC). Updated automatically on every merge. */
	@Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime updatedAt;

	@PrePersist
	protected void onPrePersist() {
		OffsetDateTime now = OffsetDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	protected void onPreUpdate() {
		this.updatedAt = OffsetDateTime.now();
	}
}