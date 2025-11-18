package org.opendevstack.apiservice.core.security;

import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;

/**
 * Custom method security expression handler for custom security expressions.
 *
 * In Spring Security 6.x, custom methods in SecurityExpressionRoot are automatically
 * recognized without needing to override createSecurityExpressionRoot().
 *
 * This class extends DefaultMethodSecurityExpressionHandler to ensure our
 * custom SecurityExpressionRoot is used for method security expressions.
 *
 * Note: In Spring Security 6.x, the createSecurityExpressionRoot() method signature
 * changed and MethodInvocation was removed. Custom expression methods defined in
 * SecurityExpressionRoot are automatically available in SpEL expressions.
 */
public class CustomMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {
    // No overrides needed in Spring Security 6.x - custom methods in
    // SecurityExpressionRoot are automatically recognized
}
