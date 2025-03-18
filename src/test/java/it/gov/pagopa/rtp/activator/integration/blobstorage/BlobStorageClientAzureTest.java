package it.gov.pagopa.rtp.activator.integration.blobstorage;

import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredential;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import it.gov.pagopa.rtp.activator.configuration.BlobStorageConfig;
import it.gov.pagopa.rtp.activator.domain.registryfile.ServiceProvider;
import it.gov.pagopa.rtp.activator.domain.registryfile.TechnicalServiceProvider;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlobStorageClientAzureTest {

    @Mock
    private BlobStorageConfig blobStorageConfig;

    @Mock
    private BlobServiceClientBuilder blobServiceClientBuilder;

    @Mock
    private BlobServiceClient blobServiceClient;

    @Mock
    private BlobContainerClient blobContainerClient;

    @Mock
    private BlobClient blobClient;

    @Mock
    private BinaryData binaryData;

    private BlobStorageClientAzure blobStorageClientAzure;

    @BeforeEach
    void setUp() {
        // Mock configuration
        when(blobStorageConfig.storageAccountName()).thenReturn("teststorage");
        when(blobStorageConfig.containerName()).thenReturn("testcontainer");
        when(blobStorageConfig.blobName()).thenReturn("testblob.json");

        // Mock the builder chain
        when(blobServiceClientBuilder.endpoint(anyString())).thenReturn(blobServiceClientBuilder);
        when(blobServiceClientBuilder.credential(any(DefaultAzureCredential.class))).thenReturn(blobServiceClientBuilder);
        when(blobServiceClientBuilder.buildClient()).thenReturn(blobServiceClient);

        // Create test instance
        blobStorageClientAzure = new BlobStorageClientAzure(blobStorageConfig, blobServiceClientBuilder);
    }

    @Test
    void getServiceProviderData_Success() {
        // Prepare test data
        ServiceProviderDataResponse expectedResponse = new ServiceProviderDataResponse(
            List.of(
                new TechnicalServiceProvider("08992631005", "CBI S.c.p.a.", "https://api.cbi.it", "6A7672BD13DAEEBEA96A2D1D", null, true),
                new TechnicalServiceProvider("BPPIITRRXXX", "Poste Italiane", "https://api.poste.it", "...", null, true)
            ),
            List.of(
                new ServiceProvider("UNCRITMM", "UniCredit S.p.A.", "08992631005"),
                new ServiceProvider("BPPIITRRXXX", "Poste Italiane S.p.A.", "BPPIITRRXXX")
            )
        );
        
        // Mock the blob storage chain
        when(blobServiceClient.getBlobContainerClient(anyString())).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.downloadContent()).thenReturn(binaryData);
        when(binaryData.toObject(ServiceProviderDataResponse.class)).thenReturn(expectedResponse);

        // Test
        Mono<ServiceProviderDataResponse> result = blobStorageClientAzure.getServiceProviderData();

        // Verify
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .verifyComplete();

        // Verify interactions
        verify(blobServiceClient).getBlobContainerClient("testcontainer");
        verify(blobContainerClient).getBlobClient("testblob.json");
        verify(blobClient).downloadContent();
        verify(binaryData).toObject(ServiceProviderDataResponse.class);
    }

    @Test
    void getServiceProviderData_Error() {
        // Mock error scenario
        when(blobServiceClient.getBlobContainerClient(anyString()))
                .thenThrow(new RuntimeException("Test error"));

        // Test
        Mono<ServiceProviderDataResponse> result = blobStorageClientAzure.getServiceProviderData();

        // Verify
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        // Verify interactions
        verify(blobServiceClient).getBlobContainerClient("testcontainer");
        verify(blobContainerClient, never()).getBlobClient(anyString());
        verify(blobClient, never()).downloadContent();
    }
}