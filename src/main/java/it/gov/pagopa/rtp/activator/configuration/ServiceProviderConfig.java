package it.gov.pagopa.rtp.activator.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "service-provider")
public record ServiceProviderConfig(
    String baseUrl,
    Activation activation,
    Send send
) {

  public record Activation(String apiVersion) {

  }

  public record Send(
      String epcMockUrl,
      Retry retry,
      Long timeout
  ) {

    public record Retry(
        long maxAttempts,
        long backoffMinDuration,
        double backoffJitter
    ) {

    }
  }
}