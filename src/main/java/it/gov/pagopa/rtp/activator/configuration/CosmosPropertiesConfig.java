package it.gov.pagopa.rtp.activator.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "cosmos.credential")
@Getter
@Setter
public class CosmosPropertiesConfig {
    private String dbName;
    private String endpoint;

    public CosmosPropertiesConfig(String dbName, String endpoint) {
        this.dbName = dbName;
        this.endpoint = endpoint;
    }
}
