package it.gov.pagopa.rtp.activator.integration.blobstorage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredential;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import it.gov.pagopa.rtp.activator.configuration.BlobStorageConfig;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class BlobStorageClientAzureTest {

  @Mock
  private BlobStorageConfig blobStorageConfig;

  @Mock
  private BlobServiceClient blobServiceClient;

  @Mock
  private BlobContainerClient blobContainerClient;

  @Mock
  private BlobServiceClientBuilder blobServiceClientBuilder;

  @Mock
  private BlobClient blobClient;

  @InjectMocks
  private BlobStorageClientAzure blobStorageClient;

  @BeforeEach
  void setup() {
    blobStorageClient = new BlobStorageClientAzure(blobStorageConfig, blobServiceClientBuilder);
  }

  @Test
  void testGetServiceProviderData() {
    when(blobStorageConfig.storageAccountName()).thenReturn("mystorage");
    when(blobStorageConfig.containerName()).thenReturn("mycontainer");
    when(blobStorageConfig.blobName()).thenReturn("myblob.json");

    when(blobServiceClientBuilder.endpoint(any(String.class)))
        .thenReturn(blobServiceClientBuilder);
    when(blobServiceClientBuilder.credential(any(DefaultAzureCredential.class)))
        .thenReturn(blobServiceClientBuilder);
    when(blobServiceClientBuilder.buildClient())
        .thenReturn(blobServiceClient);

    when(blobServiceClient.getBlobContainerClient(anyString()))
        .thenReturn(blobContainerClient);
    when(blobContainerClient.getBlobClient(anyString()))
        .thenReturn(blobClient);

    BinaryData res = BinaryData.fromString("test");
    when(blobClient.downloadContent())
        .thenReturn(res);

    StepVerifier.create(blobStorageClient.getServiceProviderData())
        .expectNext(res)
        .verifyComplete();
  }
}
