package it.gov.pagopa.rtp.activator.configuration;

import com.azure.cosmos.CosmosClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CosmosDBConfigTest {

    @Mock
    private CosmosPropertiesConfig cosmosPropertiesConfig;

    @InjectMocks
    private CosmosDBConfig cosmosDBConfig;

    @BeforeEach
    void setUp() {
        lenient().when(cosmosPropertiesConfig.getDatabase()).thenReturn("test-db");
        lenient().when(cosmosPropertiesConfig.getUri()).thenReturn("https://test-endpoint:443/");
    }

    @Test
    void testGetDatabaseName() {
        String database = cosmosDBConfig.getDatabaseName();
        assertEquals("test-db", database);
    }

    @Test
    void testGetCosmosClientBuilder() {
        CosmosClientBuilder builder = cosmosDBConfig.getCosmosClientBuilder();
        verify(cosmosPropertiesConfig).getUri();
        assertNotNull(builder);
    }
}
