package org.opendevstack.apiservice.externalservice.projectsinfoservice.annotations;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheableWithFallbackAspectTest {

    @Mock
    CacheManager cacheManager;

    @InjectMocks
    CacheableWithFallbackAspect cacheableWithFallbackAspect;

    @Test
    void givenAProceedingJoinPoint_andNoArgsMethod_whenGenerateKey_thenReturnSignatureNameKey() {
        // given
        var signatureName = "pjp-signature-name";

        ProceedingJoinPoint pjp = Mockito.mock(ProceedingJoinPoint.class);
        Signature signature = Mockito.mock(Signature.class);

        when(pjp.getArgs()).thenReturn(new Object[]{});
        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn(signatureName);

        // when
        var key = cacheableWithFallbackAspect.generateKey(pjp);

        // then
        assertThat(key).isEqualTo(signatureName);
    }

    @Test
    void givenAProceedingJoinPoint_andNoArgsMethod_whenGenerateKey_thenReturnGeneratorGeneratedKey() {
        // given
        var signatureName = "pjp-signature-name";
        Object[] args = new Object[]{"arg1", 2, 3.0};
        var expectedKey = new SimpleKey(args);

        ProceedingJoinPoint pjp = Mockito.mock(ProceedingJoinPoint.class);
        Signature signature = Mockito.mock(Signature.class);

        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn(signatureName);
        when(pjp.getArgs()).thenReturn(args);

        // when
        var key = cacheableWithFallbackAspect.generateKey(pjp);

        // then
        assertThat(key).isEqualTo(expectedKey);
    }

    @Test
    void givenAnEmptyResultValue_whenGetDefaultValue_thenReturnNull() {
        // given
        String defaultValue = "";
        Class<?> returnType = String.class;

        // when
        Object resolveDefaultValue = cacheableWithFallbackAspect.resolveDefaultValue(defaultValue, returnType);

        // then
        assertThat(resolveDefaultValue).isNull();
    }

    @Test
    void givenAnSpelReturnValue_AndTypeMap_whenGetDefaultValue_thenReturnProperType() {
        // given
        String defaultValue = "T(java.util.Collections).emptyMap()";
        Class<?> returnType = Map.class;

        // when
        Object resolveDefaultValue = cacheableWithFallbackAspect.resolveDefaultValue(defaultValue, returnType);

        // then
        assertThat(resolveDefaultValue)
                .isInstanceOf(Map.class)
                .isEqualTo(Map.of());
    }

    @Test
    void givenAnSpelReturnValue_AndTypeList_whenGetDefaultValue_andSpelExpectsMap_thenThrowException() {
        // given
        String defaultValue = "T(java.util.Collections).emptyMap()";
        Class<?> returnType = List.class;

        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> cacheableWithFallbackAspect.resolveDefaultValue(defaultValue, returnType));

        // then
        assertThat(exception.getMessage()).isEqualTo("Default value type mismatch: expected java.util.List, but got java.util.Collections$EmptyMap");
    }

    @Test
    void givenAProceedingJoinPoint_andACacheableWithFallback_whenCacheWithFallback_andNoValueIsCached_thenCallRealMethod() throws Throwable {
        // given
        String primary = "primaryCache";
        String fallback = "fallbackCache";
        String defaultValue = "";
        String signatureName = "pjp-signature-name";
        String expectedResult = "real-method-result";

        ProceedingJoinPoint pjp = initializeProceedingJoinPoint(signatureName);
        CacheableWithFallback cacheableWithFallback = initializeCacheableWithFallback(primary, fallback, defaultValue);

        when(cacheManager.getCache(primary)).thenReturn(null);
        when(cacheManager.getCache(fallback)).thenReturn(null);

        when(pjp.proceed()).thenReturn(expectedResult);

        // when
        var result = cacheableWithFallbackAspect.cacheWithFallback(pjp, cacheableWithFallback);

        // then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void givenAProceedingJoinPoint_andACacheableWithFallback_whenCacheWithFallback_andResponseIsCachedInPrimary_thenReturnPrimaryCacheValue() {
        // given
        String primary = "primaryCache";
        String fallback = "fallbackCache";
        String defaultValue = "";
        String signatureName = "pjp-signature-name";
        String primaryCacheResult = "primary-cache-result";

        ProceedingJoinPoint pjp = initializeProceedingJoinPoint(signatureName);
        CacheableWithFallback cacheableWithFallback = initializeCacheableWithFallback(primary, fallback, defaultValue);

        initializeCache(primary, primaryCacheResult);

        // when
        var result = cacheableWithFallbackAspect.cacheWithFallback(pjp, cacheableWithFallback);

        // then
        assertThat(result).isEqualTo(primaryCacheResult);
    }

    @Test
    void givenAProceedingJoinPoint_andACacheableWithFallback_whenCacheWithFallback_andPrimaryCacheIsEmpty_thenReturnRealMethod() throws Throwable {
        // given
        String primary = "primaryCache";
        String fallback = "fallbackCache";
        String defaultValue = "";
        String signatureName = "pjp-signature-name";
        String realMethodResult = "real-method-result";

        ProceedingJoinPoint pjp = initializeProceedingJoinPoint(signatureName);
        CacheableWithFallback cacheableWithFallback = initializeCacheableWithFallback(primary, fallback, defaultValue);

        when(pjp.proceed()).thenReturn(realMethodResult);

        // when
        var result = cacheableWithFallbackAspect.cacheWithFallback(pjp, cacheableWithFallback);

        // then
        assertThat(result).isEqualTo(realMethodResult);
    }

    @Test
    void givenAProceedingJoinPoint_andACacheableWithFallback_whenCacheWithFallback_andPrimaryCacheIsEmpty_AndRealCallFails_AndResponseIsCachedInFallback_thenReturnFallbackCacheValue() throws Throwable {
        // given
        String primary = "primaryCache";
        String fallback = "fallbackCache";
        String defaultValue = "";
        String signatureName = "pjp-signature-name";
        String fallbackCacheResult = "fallback-cache-result";

        ProceedingJoinPoint pjp = initializeProceedingJoinPoint(signatureName);
        CacheableWithFallback cacheableWithFallback = initializeCacheableWithFallback(primary, fallback, defaultValue);

        initializeCache(primary, null);
        initializeCache(fallback, fallbackCacheResult);

        when(pjp.proceed()).thenThrow(new RuntimeException("That's an expected exception"));

        // when
        var result = cacheableWithFallbackAspect.cacheWithFallback(pjp, cacheableWithFallback);

        // then
        assertThat(result).isEqualTo(fallbackCacheResult);
    }

    @Test
    void givenAProceedingJoinPoint_andACacheableWithFallback_whenCacheWithFallback_andNoValueIsCached_andRealCallFails_thenReturnDefaultValue() throws Throwable {
        // given
        String primary = "primaryCache";
        String fallback = "fallbackCache";
        String defaultValue = "";
        String signatureName = "pjp-signature-name";

        ProceedingJoinPoint pjp = initializeProceedingJoinPoint(signatureName);
        CacheableWithFallback cacheableWithFallback = initializeCacheableWithFallback(primary, fallback, defaultValue);

        when(pjp.proceed()).thenThrow(new RuntimeException("That's an expected exception"));

        // when
        var result = cacheableWithFallbackAspect.cacheWithFallback(pjp, cacheableWithFallback);

        // then
        assertThat(result).isEqualTo(defaultValue);
    }

    private void initializeCache(String cacheName, Object cacheResult) {
        Cache cache = Mockito.mock(Cache.class);
        Cache.ValueWrapper cacheValueWrapper = Mockito.mock(Cache.ValueWrapper.class);

        when(cacheManager.getCache(cacheName)).thenReturn(cache);
        when(cache.get(any())).thenReturn(cacheValueWrapper);
        when(cacheValueWrapper.get()).thenReturn(cacheResult);
    }

    private ProceedingJoinPoint initializeProceedingJoinPoint(String signatureName) {
        ProceedingJoinPoint pjp = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        Method method = Mockito.mock(Method.class);

        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn(signatureName);
        when(signature.getMethod()).thenReturn(method);
        Mockito.doReturn(String.class).when(method).getReturnType(); // Mockito when method is not dealing well with generics

        return pjp;
    }

    private CacheableWithFallback initializeCacheableWithFallback(String primary, String fallback, String defaultValue) {
        CacheableWithFallback cacheableWithFallback = Mockito.mock(CacheableWithFallback.class);

        when(cacheableWithFallback.primary()).thenReturn(primary);
        when(cacheableWithFallback.fallback()).thenReturn(fallback);
        when(cacheableWithFallback.defaultValue()).thenReturn(defaultValue);

        return cacheableWithFallback;
    }
}
