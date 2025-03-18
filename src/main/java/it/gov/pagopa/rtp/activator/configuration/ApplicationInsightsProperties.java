package it.gov.pagopa.rtp.activator.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "applicationinsights")
public record ApplicationInsightsProperties(String connectionString) {
    
}
