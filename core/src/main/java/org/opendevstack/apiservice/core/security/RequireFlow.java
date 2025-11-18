package org.opendevstack.apiservice.core.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify required OAuth2 flow for an endpoint
 * Can be used on classes or methods
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireFlow {

    /**
     * Required OAuth2 flow(s)
     * Values: authorization-code, client-credentials, on-behalf-of
     */
    String[] value() default {};

    /**
     * Whether actor claim is required (for On-Behalf-Of flow)
     */
    boolean requireActor() default false;

    /**
     * Required delegation depth
     */
    int requireDelegationDepth() default 0;

    /**
     * Required scopes
     */
    String[] scopes() default {};

    /**
     * Error message if validation fails
     */
    String message() default "Insufficient OAuth2 flow permissions";
}
