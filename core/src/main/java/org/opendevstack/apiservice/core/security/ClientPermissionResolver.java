package org.opendevstack.apiservice.core.security;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opendevstack.apiservice.persistence.entity.RoleApiPermissionEntity;
import org.opendevstack.apiservice.persistence.repository.ClientAppRepository;
import org.opendevstack.apiservice.persistence.repository.RoleApiPermissionRepository;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Resolves effective API permissions for a given Azure AD client identifier by
 * querying the authorization tables ({@code client_apps → client_app_roles →
 * roles → role_api_permissions → api_resources}).
 * <p>
 * Results are cached in-memory (simple TTL map) to avoid a DB round-trip on every
 * request.  Cache is invalidated automatically after {@value #CACHE_TTL_MINUTES}
 * minutes.
 */
@Slf4j
@Service
@AllArgsConstructor
public class ClientPermissionResolver {

	private static final long CACHE_TTL_MINUTES = 5;

	private final ClientAppRepository clientAppRepository;
	private final RoleApiPermissionRepository roleApiPermissionRepository;

	private final Map<String, CachedEntry> cache = new ConcurrentHashMap<>();


	/**
	 * Resolve permissions for the given client.  Returns
	 * {@link ClientPermissions#ANONYMOUS} when the client is unknown or disabled.
	 */
	public ClientPermissions resolve(String clientId) {
		if (clientId == null || clientId.isBlank()) {
			return ClientPermissions.ANONYMOUS;
		}

		CachedEntry cached = cache.get(clientId);
		if (cached != null && !cached.isExpired()) {
			return cached.permissions;
		}

		ClientPermissions permissions = loadFromDatabase(clientId);
		cache.put(clientId, new CachedEntry(permissions));
		return permissions;
	}

	/** Evict all cached entries (e.g. after admin changes). */
	public void evictAll() {
		cache.clear();
	}

	/** Evict a specific client from the cache. */
	public void evict(String clientId) {
		cache.remove(clientId);
	}

	// ── internal ─────────────────────────────────────────────────────────

	private ClientPermissions loadFromDatabase(String clientId) {
		boolean exists = clientAppRepository.findByClientId(clientId)
				.map(ca -> ca.isEnabled())
				.orElse(false);

		if (!exists) {
			log.debug("Client '{}' not found or disabled", clientId);
			return new ClientPermissions(clientId, false, List.of(), List.of(), List.of());
		}

		List<RoleApiPermissionEntity> perms = roleApiPermissionRepository.findByClientId(clientId);

		List<String> roles = perms.stream()
				.map(p -> p.getRole().getName())
				.distinct()
				.toList();

		List<ClientPermissions.ApiGrant> grants = new ArrayList<>();
		List<ClientPermissions.ApiGrant> denials = new ArrayList<>();

		for (RoleApiPermissionEntity perm : perms) {
			var grant = new ClientPermissions.ApiGrant(
					perm.getApiResource().getHttpMethod(),
					perm.getApiResource().getPattern());
			if (perm.isAllowed()) {
				grants.add(grant);
			}
			else {
				denials.add(grant);
			}
		}

		log.debug("Client '{}': roles={}, grants={}, denials={}", clientId, roles, grants.size(),
				denials.size());
		return new ClientPermissions(clientId, true, roles, grants, denials);
	}

	// ── simple TTL cache entry ───────────────────────────────────────────

	private static final class CachedEntry {

		final ClientPermissions permissions;

		final long expiresAt;

		CachedEntry(ClientPermissions permissions) {
			this.permissions = permissions;
			this.expiresAt = System.nanoTime() + TimeUnit.MINUTES.toNanos(CACHE_TTL_MINUTES);
		}

		boolean isExpired() {
			return System.nanoTime() > expiresAt;
		}

	}

}
