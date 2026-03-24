package org.opendevstack.apiservice.core.engine.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.opendevstack.apiservice.core.contracts.registry.ApiDefinition;
import org.opendevstack.apiservice.core.engine.registry.ApiDefinitionResolver;
import org.opendevstack.apiservice.core.security.filter.AuthTypeEnforcementFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class ApiRegistryFilter extends OncePerRequestFilter {

    private final ApiDefinitionResolver resolver;

    public ApiRegistryFilter(ApiDefinitionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Optional<ApiDefinition> apiDef = resolver.resolve(request);

        if (apiDef.isPresent()) {
            ApiDefinition def = apiDef.get();
            if (!def.isEnabled()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "API is disabled");
                return;
            }
            request.setAttribute(AuthTypeEnforcementFilter.API_DEFINITION_ATTR, def);
        }

        filterChain.doFilter(request, response);
    }
}
