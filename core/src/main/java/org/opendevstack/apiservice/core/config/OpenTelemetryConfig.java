package org.opendevstack.apiservice.core.config;

import org.springframework.context.annotation.Configuration;

/**
 * OpenTelemetry configuration for the application.
 * 
 * This configuration relies on the OpenTelemetry Spring Boot starter
 * for auto-configuration. All OpenTelemetry settings are configured
 * through application.properties using the standard otel.* properties.
 * 
 * The starter automatically provides:
 * - Trace instrumentation for Spring Web MVC
 * - OTLP exporter configuration
 * - Resource detection and attributes
 * - Sampling configuration
 */
@Configuration
public class OpenTelemetryConfig {
    
    // OpenTelemetry auto-configuration is handled by the Spring Boot starter
    // Configuration is done through application.properties
    
}