package it.gov.pagopa.rtp.activator.configuration;

import com.azure.monitor.opentelemetry.autoconfigure.AzureMonitorAutoConfigure;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppinsightsConfig {

  private final String connectionString;


  public AppinsightsConfig(@Value("${spring.cloud.azure.monitor.connection-string}") String connectionString) {
    this.connectionString = connectionString;
  }

  @Bean
  public OpenTelemetry configureAzureMonitorExporter() {
    AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

    AzureMonitorAutoConfigure.customize(sdkBuilder, connectionString);

    return sdkBuilder.build().getOpenTelemetrySdk();
  }

}
