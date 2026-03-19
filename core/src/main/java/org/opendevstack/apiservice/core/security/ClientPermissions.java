package org.opendevstack.apiservice.core.security;

import java.util.List;

/**
 * Resolved effective permissions for a client application.
 *
 * @param clientId  Azure AD application/client UUID
 * @param enabled   whether the client is registered and enabled
 * @param roles     role names assigned to the client in DB
 * @param grants    API patterns the client is allowed to access
 * @param denials   API patterns explicitly denied (deny wins over grant)
 */
public record ClientPermissions(
		String clientId,
		boolean enabled,
		List<String> roles,
		List<ApiGrant> grants,
		List<ApiGrant> denials) {

	public static final ClientPermissions ANONYMOUS =
			new ClientPermissions(null, false, List.of(), List.of(), List.of());

	/**
	 * An individual API grant or denial.
	 *
	 * @param httpMethod HTTP method ({@code *} matches all)
	 * @param pattern    Ant-style URL pattern
	 */
	public record ApiGrant(String httpMethod, String pattern) {
	}

}
