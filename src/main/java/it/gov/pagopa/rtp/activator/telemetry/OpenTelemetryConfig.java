package it.gov.pagopa.rtp.activator.telemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenTelemetry setup.
 * Provides the necessary beans for OpenTelemetry tracing functionality
 * in the RTP Activator application.
 */

@Configuration
public class OpenTelemetryConfig {

    /**
     * Creates and configures the OpenTelemetry Tracer bean.
     * This tracer is used throughout the application for creating
     * and managing trace spans for MongoDB operations.
     *
     * @param openTelemetry The OpenTelemetry instance injected by Spring
     * @return Configured Tracer instance for the RTP Activator application
     */
    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(
                "rtp-activator", // Instrumentation name
                "1.0.0"// Instrumentation version
        );
    }
}