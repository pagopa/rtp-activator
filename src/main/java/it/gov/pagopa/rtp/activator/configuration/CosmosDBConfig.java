package it.gov.pagopa.rtp.activator.configuration;

import org.springframework.context.annotation.Configuration;

import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;

@Configuration
@EnableReactiveCosmosRepositories("it.gov.pagopa.rtp.activator.repository")
public class CosmosDBConfig {
}
