package it.gov.pagopa.rtp.activator.telemetry;

import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporter;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for AppInsight OpenTelemetry. Connection string will
 * automatically take from
 * environment variable APPLICATIONINSIGHTS_CONNECTION_STRING
 */
@Configuration
@ConditionalOnProperty(value = "applicationinsights.enabled", havingValue = "true", matchIfMissing = false)
public class AppInsightConfig implements BeanPostProcessor {

  @Bean
  public AutoConfigurationCustomizerProvider otelCustomizer(
      @Value("${applicationinsights.connection-string}") String applicationInsights) {
    return p -> {
      if (p instanceof AutoConfiguredOpenTelemetrySdkBuilder) {
        AzureMonitorExporter.customize(p, applicationInsights);
      }
    };
  }

}
