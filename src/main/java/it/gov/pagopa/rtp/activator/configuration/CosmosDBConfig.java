package it.gov.pagopa.rtp.activator.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;

@Configuration
@EnableReactiveCosmosRepositories("it.gov.pagopa.rtp.activator.repository")
public class CosmosDBConfig extends AbstractCosmosConfiguration {

    private CosmosPropertiesConfig cosmosPropertiesConfig;

    public CosmosDBConfig(CosmosPropertiesConfig cosmosPropertiesConfig){
        this.cosmosPropertiesConfig = cosmosPropertiesConfig;
    }

    @Override
    protected String getDatabaseName() {
        return cosmosPropertiesConfig.getDatabase();
    }

    @Bean
    public CosmosClientBuilder getCosmosClientBuilder() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
                .build();

        return new CosmosClientBuilder()
            .endpoint(cosmosPropertiesConfig.getUri())
            .credential(credential);
    }

}
