package org.opendevstack.apiservice.externalservice.projectsinfoservice.annotations;

import org.opendevstack.apiservice.externalservice.projectsinfoservice.config.CacheConfiguration;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheableWithFallback {
    String primary();
    String fallback();
    String defaultValue() default "";  // SpEL or literal string
    String cacheManager() default CacheConfiguration.CUSTOM_CACHE_MANAGER_NAME;
}
