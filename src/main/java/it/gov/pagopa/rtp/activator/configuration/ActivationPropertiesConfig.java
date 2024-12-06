package it.gov.pagopa.rtp.activator.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "activation")
public record ActivationPropertiesConfig(
    String baseUrl) {

}
