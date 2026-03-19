package org.opendevstack.apiservice.core.security;

import java.util.function.Supplier;

import org.opendevstack.apiservice.core.config.EntraIdRoleConverter;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.util.AntPathMatcher;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom {@link AuthorizationManager} that enforces DB-driven client
 * permissions on top of the standard Entra ID JWT authentication.
 * <p>
 * Decision logic (fail-closed):
 * <ol>
 *   <li>Extract {@code client_id} from the JWT ({@code azp} / {@code appid}).</li>
 *   <li>Resolve the client's effective permissions from the database (cached).</li>
 *   <li>If the client is unknown / disabled → <strong>deny</strong>.</li>
 *   <li>If an explicit <em>deny</em> rule matches the request → <strong>deny</strong>.</li>
 *   <li>If an <em>allow</em> rule matches the request → <strong>grant</strong>.</li>
 *   <li>Otherwise → <strong>deny</strong> (fail-closed).</li>
 * </ol>
 * <p>
 * This manager is intended to be composed with Spring Security's default chain
 * using {@code .access()} in the {@code SecurityFilterChain} configuration.
 */
@Slf4j
public class ClientApiAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

	private final ClientPermissionResolver permissionResolver;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	public ClientApiAuthorizationManager(ClientPermissionResolver permissionResolver) {
		this.permissionResolver = permissionResolver;
	}

	@Override
	public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier,
			RequestAuthorizationContext context) {

		Authentication authentication = authenticationSupplier.get();
		HttpServletRequest request = context.getRequest();
		String requestUri = request.getRequestURI();
		String httpMethod = request.getMethod();

		// Not authenticated at all → deny
		if (authentication == null || !authentication.isAuthenticated()) {
			log.debug("DENY {} {} — not authenticated", httpMethod, requestUri);
			return new AuthorizationDecision(false);
		}

		// Not a JWT-based principal → deny
		if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
			log.debug("DENY {} {} — principal is not a JWT", httpMethod, requestUri);
			return new AuthorizationDecision(false);
		}

		String clientId = EntraIdRoleConverter.extractClientId(jwt);
		if (clientId == null || clientId.isBlank()) {
			log.debug("DENY {} {} — no client_id in token", httpMethod, requestUri);
			return new AuthorizationDecision(false);
		}

		ClientPermissions permissions = permissionResolver.resolve(clientId);

		// Unknown or disabled client → deny
		if (!permissions.enabled()) {
			log.debug("DENY {} {} — client '{}' not registered or disabled", httpMethod, requestUri,
					clientId);
			return new AuthorizationDecision(false);
		}

		// Explicit deny wins
		for (ClientPermissions.ApiGrant denial : permissions.denials()) {
			if (matchesRequest(denial, httpMethod, requestUri)) {
				log.debug("DENY {} {} — explicit deny for client '{}'", httpMethod, requestUri,
						clientId);
				return new AuthorizationDecision(false);
			}
		}

		// Check for matching grant
		for (ClientPermissions.ApiGrant grant : permissions.grants()) {
			if (matchesRequest(grant, httpMethod, requestUri)) {
				log.debug("GRANT {} {} — client '{}', matched pattern {}:{}", httpMethod,
						requestUri, clientId, grant.httpMethod(), grant.pattern());
				return new AuthorizationDecision(true);
			}
		}

		// Fail-closed: no matching rule → deny
		log.debug("DENY {} {} — no matching permission for client '{}'", httpMethod, requestUri,
				clientId);
		return new AuthorizationDecision(false);
	}

	private boolean matchesRequest(ClientPermissions.ApiGrant grant, String httpMethod,
			String requestUri) {
		boolean methodMatch = "*".equals(grant.httpMethod())
				|| grant.httpMethod().equalsIgnoreCase(httpMethod);
		return methodMatch && pathMatcher.match(grant.pattern(), requestUri);
	}

}
