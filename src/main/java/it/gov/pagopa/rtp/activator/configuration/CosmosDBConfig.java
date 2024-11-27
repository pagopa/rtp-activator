package it.gov.pagopa.rtp.activator.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;

@Configuration
@EnableReactiveCosmosRepositories("it.gov.pagopa.rtp.activator.repository")
public class CosmosDBConfig {

    @Bean
    public AzureGlobalProperties azureGlobalProperties() {
        return new AzureGlobalProperties();
    }

}
