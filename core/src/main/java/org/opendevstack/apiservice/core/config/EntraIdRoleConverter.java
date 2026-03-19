package org.opendevstack.apiservice.core.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Converts JWT claims into Spring Security {@link GrantedAuthority} instances.
 * <p>
 * Supports <strong>Azure Entra ID</strong> tokens.
 * <p>
 * Entra ID tokens carry:
 * <ul>
 *   <li>{@code roles} — flat string array of App Roles assigned to the principal</li>
 *   <li>{@code scp} — space-delimited delegated scopes (authorization-code / OBO flows)</li>
 * </ul>
 * All roles are mapped to {@code ROLE_<name>} authorities; scopes are mapped to
 * {@code SCOPE_<name>} authorities.
 */
public class EntraIdRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String ROLES_CLAIM = "roles";

    @Override
    @SuppressWarnings("nullness")
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        if (jwt == null) {
            return List.of();
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        // ── Entra ID: flat "roles" array (App Roles) Client Credentials
        extractEntraRoles(jwt, authorities);

        // ── Entra ID: "scp" claim (delegated scopes, space-separated, on behalf of a user)
        extractEntraScopes(jwt, authorities);

        return List.copyOf(authorities);
    }

    /**
     * Extracts application roles from the "roles" claim and adds them as authorities.
     * <p>
     * This information will come in client-credentials flows where the client acts on its own behalf (no user).
     * For example, a role "Admin" would be mapped to an authority "ROLE_Admin".
     * @param jwt the JWT token containing the claims
     * @param authorities the list of authorities to which the extracted roles will be added
     */
    private void extractEntraRoles(Jwt jwt, List<GrantedAuthority> authorities) {
        Object rolesObj = jwt.getClaim(ROLES_CLAIM);
        if (rolesObj instanceof List<?> rolesList) {
            for (Object role : rolesList) {
                if (role instanceof String r && !r.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + r));
                }
            }
        }
    }
    
    
    /**
     * Extracts delegated scopes from the "scp" claim and adds them as authorities.
     * This information will come in authorization-code or OBO flows where the client acts on behalf of a user.
     * <p>
     * For example, a scope "read:projects" would be mapped to an authority "SCOPE_read:projects".
     * @param jwt the JWT token containing the claims
     * @param authorities the list of authorities to which the extracted scopes will be added
     */
    private void extractEntraScopes(Jwt jwt, List<GrantedAuthority> authorities) {
        String scp = jwt.getClaimAsString("scp");
        if (scp != null && !scp.isBlank()) {
            for (String scope : scp.split(" ")) {
                if (!scope.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
                }
            }
        }
    }

    /**
     * Extracts the client application identifier from the JWT.
     * <p>
     * Entra ID uses {@code azp} (v2 tokens) or {@code appid} (v1 tokens).
     * Returns {@code null} when neither claim is present.
     */
    public static String extractClientId(Jwt jwt) {
        String clientId = jwt.getClaimAsString("azp");
        if (clientId == null || clientId.isBlank()) {
            clientId = jwt.getClaimAsString("appid");
        }
        return clientId;
    }
}
