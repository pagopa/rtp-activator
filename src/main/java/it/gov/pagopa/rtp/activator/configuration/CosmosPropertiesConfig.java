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
    private String key;

    public CosmosPropertiesConfig(String database, String uri, String key) {
        this.database = database;
        this.uri = uri;
        this.key = key;
    }
}
