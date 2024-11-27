package it.gov.pagopa.rtp.activator.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = CosmosPropertiesConfig.class)
@TestPropertySource("classpath:application.properties")
class CosmosPropertiesConfigTest {

    @Autowired
    private CosmosPropertiesConfig cosmosPropertiesConfig;

    @Test
    void testPropertiesLoaded() {
        assertNotNull(cosmosPropertiesConfig);
        assertEquals("https://example.com/db/endpoint", cosmosPropertiesConfig.getEndpoint());
        assertEquals("rtp", cosmosPropertiesConfig.getDbName());

    }
}