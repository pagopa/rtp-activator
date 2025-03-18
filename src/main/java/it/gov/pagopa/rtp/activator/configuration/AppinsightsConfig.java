package it.gov.pagopa.rtp.activator.configuration;

import com.azure.monitor.opentelemetry.autoconfigure.AzureMonitorAutoConfigure;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppinsightsConfig {

  private final ApplicationInsightsProperties applicationInsightsProperties;

  public AppinsightsConfig(ApplicationInsightsProperties applicationInsightsProperties) {
    this.applicationInsightsProperties = applicationInsightsProperties;
  }

  @Bean
  public OpenTelemetry configureAzureMonitorExporter() {
    AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

    AzureMonitorAutoConfigure.customize(sdkBuilder, applicationInsightsProperties.connectionString());

    return sdkBuilder.build().getOpenTelemetrySdk();
  }

}
