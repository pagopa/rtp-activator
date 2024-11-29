package it.gov.pagopa.rtp.activator.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "activation")
@Getter
@Setter
public class ActivationPropertiesConfig{
    private String baseUrl;

    public ActivationPropertiesConfig(String baseUrl){
        this.baseUrl = baseUrl;
    }
}
