package org.opendevstack.apiservice.core.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CachedBodyRequestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (shouldWrap(request)) {
            filterChain.doFilter(new CachedBodyHttpServletRequest(request), response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldWrap(HttpServletRequest request) {
        String method = request.getMethod();
        if (!"POST".equalsIgnoreCase(method)
                && !"PUT".equalsIgnoreCase(method)
                && !"PATCH".equalsIgnoreCase(method)) {
            return false;
        }

        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("application/json");
    }
}
