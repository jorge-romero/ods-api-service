package org.opendevstack.apiservice.core.engine.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Eagerly reads and caches the request body so it can be consumed multiple times
 * (policy evaluators + downstream controllers).
 *
 * <p>Registered explicitly in the {@code SecurityFilterChain} by
 * {@link org.opendevstack.apiservice.core.config.SecurityConfig} —
 * do <b>not</b> annotate with {@code @Component}.
 */
public class RequestBodyCachingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(new CachedBodyHttpServletRequest(request), response);
    }
}
