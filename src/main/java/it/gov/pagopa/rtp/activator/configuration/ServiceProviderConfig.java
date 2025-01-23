package it.gov.pagopa.rtp.activator.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "service-provider")
public record ServiceProviderConfig(String apiVersion,
                                    String epcMockUrl,
                                    long maxAttempts,
                                    long backoffMinDuration,
                                    double backoffJitter) {

}
