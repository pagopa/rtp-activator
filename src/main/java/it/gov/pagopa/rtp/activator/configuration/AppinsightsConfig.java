package it.gov.pagopa.rtp.activator.configuration;

import com.azure.monitor.opentelemetry.autoconfigure.AzureMonitorAutoConfigure;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
public class AppinsightsConfig {

  private final ApplicationInsightsProperties applicationInsightsProperties;

  public AppinsightsConfig(ApplicationInsightsProperties applicationInsightsProperties) {
    this.applicationInsightsProperties = applicationInsightsProperties;
  }

  @Bean
  @Primary
  public OpenTelemetry configureAzureMonitorExporter() {
    AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();
    AzureMonitorAutoConfigure.customize(sdkBuilder, applicationInsightsProperties.connectionString());

    return sdkBuilder.build().getOpenTelemetrySdk();
  }

}
