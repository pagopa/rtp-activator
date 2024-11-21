package it.gov.pagopa.rtp.activator.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;

@Configuration
@EnableCosmosRepositories

public class CosmosConfig extends AbstractCosmosConfiguration {

    @Override
    protected String getDatabaseName() {
        //to move 
        return "<db-name>";
    }

    @Bean
    public CosmosClientBuilder getCosmosClientBuilder() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
                .build();

        return new CosmosClientBuilder()
                //endpoint to move
                .endpoint("<azure-cosmos-db-nosql-account-endpoint>")
                .credential(credential);
    }

}
