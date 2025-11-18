package org.opendevstack.apiservice.core.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String ROLES_CLAIM = "roles";

    @Override
    @SuppressWarnings("nullness")
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        if (jwt == null) {
            return List.of();
        }
        // Extract realm roles (Keycloak/Auth0 standard)
        List<String> realmRoles = getRealmRoles(jwt);

        // Extract resource roles (client-specific roles)
        List<String> resourceRoles = getResourceRoles(jwt);

        // Combine all roles
        List<String> allRoles = combineRoles(realmRoles, resourceRoles);

        // Convert to GrantedAuthority with ROLE_ prefix
        return allRoles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .map(GrantedAuthority.class::cast)
            .toList();
    }

    private List<String> getRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.containsKey(ROLES_CLAIM)) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get(ROLES_CLAIM);
            return roles != null ? roles : List.of();
        }
        return List.of();
    }

    private List<String> getResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess == null) {
            return List.of();
        }

        return resourceAccess.values().stream()
            .map(this::extractRolesFromResource)
            .flatMap(Collection::stream)
            .toList();
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRolesFromResource(Object resource) {
        if (resource instanceof Map) {
            Map<?, ?> resourceMap = (Map<?, ?>) resource;
            Object roles = resourceMap.get(ROLES_CLAIM);
            if (roles instanceof List) {
                return (List<String>) roles;
            }
        }
        return List.of();
    }

    private List<String> combineRoles(List<String> realmRoles, List<String> resourceRoles) {
        return java.util.stream.Stream.concat(
            realmRoles.stream(),
            resourceRoles.stream()
        ).toList();
    }
}
