package org.opendevstack.apiservice.externalservice.projectsinfoservice.annotations;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@AllArgsConstructor
@Slf4j
@Aspect
@Component
@Order(1)
public class CacheableWithFallbackAspect {

    private static final String EMPTY_STRING = "";
    private final CacheManager cacheManager;

    @Around("@annotation(cacheableWithFallback)")
    public Object cacheWithFallback(ProceedingJoinPoint pjp, CacheableWithFallback cacheableWithFallback) {
        log.debug("CacheWithFallback. pjp: {}, annotation: {}", pjp, cacheableWithFallback);

        String primary = cacheableWithFallback.primary();
        String fallback = cacheableWithFallback.fallback();
        Object defaultValue = getDefaultValue(pjp, cacheableWithFallback);

        Object key = generateKey(pjp);
        Cache primaryCache = cacheManager.getCache(primary);
        Cache fallbackCache = cacheManager.getCache(fallback);

        Object result = null;

        if (primaryCache != null) {
            result = extractValueFromPrimaryCache(primaryCache, key);
        }

        if (result == null) {
            Object pjpResult = extractValueFromRealMethodCall(pjp);

            if (pjpResult != null) {
                result = pjpResult;

                updateCaches(primaryCache, fallbackCache, (String) key, result);
            } else if (fallbackCache != null) {
                result = extractValueFromFallbackCache(fallbackCache, key, defaultValue);
            } else {
                result = defaultValue;

                log.debug("No fallback cache configured. Returning default value: {}", defaultValue);
            }
        }

        log.debug("CacheableWithFallback result: {}", result);
        return result;
    }

    protected Object getDefaultValue(ProceedingJoinPoint pjp, CacheableWithFallback cacheableWithFallback) {
        String defaultValueStr = cacheableWithFallback.defaultValue();
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Class<?> returnType = method.getReturnType();

        if (defaultValueStr != null && defaultValueStr.equals(EMPTY_STRING)) {
            return EMPTY_STRING;
        } else {
            return resolveDefaultValue(defaultValueStr, returnType);
        }
    }


    protected Object resolveDefaultValue(String defaultValue, Class<?> returnType) {
        if (defaultValue == null || defaultValue.isEmpty()) return null;

        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        Object result = parser.parseExpression(defaultValue).getValue(context);

        if (result != null && !returnType.isAssignableFrom(result.getClass())) {
            throw new IllegalArgumentException("Default value type mismatch: expected " +
                    returnType.getName() + ", but got " + result.getClass().getName());
        }

        return result;
    }

    protected Object generateKey(ProceedingJoinPoint pjp) {
        log.debug("Generating key for method: {}", pjp.getSignature().getName());

        Object generatedKey;
        Object[] args = pjp.getArgs();

        if (args == null || args.length == 0) {
            generatedKey = pjp.getSignature().getName();
        } else {
            generatedKey = SimpleKeyGenerator.generateKey(pjp.getArgs());
        }

        log.debug("Generated key: {}", generatedKey);

        return generatedKey;
    }

    private Object extractValueFromPrimaryCache(Cache primaryCache, Object cacheKey) {
        Object result = null;

        Cache.ValueWrapper primaryCachedValue = primaryCache.get(cacheKey);
        if (primaryCachedValue != null) {
            result = primaryCachedValue.get();

            log.debug("Returning value from primary cache: {}, key: {}", primaryCache.getName(), cacheKey);
        }

        return result;
    }

    private Object extractValueFromFallbackCache(Cache fallbackCache, Object cacheKey, Object defaultValue) {
        log.debug("There were an issue getting the real value. Trying to get value from fallback cache: {}, key: {}", fallbackCache, cacheKey);

        Object result;

        Cache.ValueWrapper fallbackCachedValue = fallbackCache.get(cacheKey);
        if (fallbackCachedValue != null) {
            result = fallbackCachedValue.get();

            log.debug("Returning value from fallback cache: {}, key: {}", fallbackCache, cacheKey);
        } else {
            result = defaultValue;

            log.debug("No value in fallback cache. Returning default value: {}", defaultValue);
        }

        return result;
    }

    private Object extractValueFromRealMethodCall(ProceedingJoinPoint pjp) {
        Object pjpResult = null;

        try {
            log.debug("Calling real method: {}", pjp.getSignature().getName());

            pjpResult = pjp.proceed(); // call the actual method
        } catch (Throwable throwable) {
            log.error("There were an error calling real method: {}", pjp.getSignature().getName(), throwable);
        }

        return pjpResult;
    }

    private void updateCaches(Cache primaryCache, Cache fallbackCache, String key, Object result) {
        log.debug("Real method call generated a result. Updating caches: {}, {}, key: {}", primaryCache, fallbackCache, key);

        if (primaryCache != null) {
            primaryCache.put(key, result);
        }

        if (fallbackCache != null) {
            fallbackCache.put(key, result);
        }

        log.debug("Caches updated. Primary: {}, Fallback: {}, key: {}", primaryCache, fallbackCache, key);
    }
}
