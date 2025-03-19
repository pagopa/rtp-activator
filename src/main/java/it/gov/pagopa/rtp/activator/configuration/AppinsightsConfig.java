package it.gov.pagopa.rtp.activator.configuration;

import com.azure.monitor.opentelemetry.autoconfigure.AzureMonitorAutoConfigure;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Role;

@Slf4j
@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class AppinsightsConfig {

  private final ApplicationInsightsProperties applicationInsightsProperties;

  public AppinsightsConfig(ApplicationInsightsProperties applicationInsightsProperties) {
    this.applicationInsightsProperties = applicationInsightsProperties;
  }

  @Bean
  @Primary
  @Lazy
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public OpenTelemetry configureAzureMonitorExporter() {
    AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();
    AzureMonitorAutoConfigure.customize(sdkBuilder, applicationInsightsProperties.connectionString());

    return sdkBuilder.build().getOpenTelemetrySdk();
  }

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
