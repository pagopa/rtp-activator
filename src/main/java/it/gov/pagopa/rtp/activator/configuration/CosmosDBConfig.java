package it.gov.pagopa.rtp.activator.configuration;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;

@Configuration
@EnableReactiveCosmosRepositories
public class CosmosDBConfig extends AbstractCosmosConfiguration {

    @Autowired
    private CosmosPropertiesConfig cosmosPropertiesConfig;

    @Override
    protected String getDatabaseName() {
        return cosmosPropertiesConfig.getDbName();
    }

    @Bean
    public CosmosClientBuilder getCosmosClientBuilder() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
                .build();

        return new CosmosClientBuilder()
                .endpoint(cosmosPropertiesConfig.getEndpoint())
                .credential(credential);
    }

}
