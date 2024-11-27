package it.gov.pagopa.rtp.activator.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "azure.cosmos")
@Getter
@Setter
public class CosmosPropertiesConfig {
    private String database;
    private String uri;

    public CosmosPropertiesConfig(String database, String uri) {
        this.database = database;
        this.uri = uri;
    }
}
