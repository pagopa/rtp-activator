package it.gov.pagopa.rtp.activator.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;

@Configuration
@ConfigurationProperties(prefix = "cosmos.credential")
@EnableCosmosRepositories
public class CosmosConfig extends AbstractCosmosConfiguration {

    private String dbName;
    private String endpoint;

    @Override
    protected String getDatabaseName() {
        return dbName;
    }

    @Bean
    public CosmosClientBuilder getCosmosClientBuilder() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
                .build();

        return new CosmosClientBuilder()
                //endpoint to move
                .endpoint(endpoint)
                .credential(credential);
    }

}
