package it.gov.pagopa.rtp.activator.integration.blobstorage;

import org.springframework.stereotype.Component;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import it.gov.pagopa.rtp.activator.configuration.BlobStorageConfig;
import reactor.core.publisher.Mono;

/*
 * Interact with the Azure Blob Storage using the Azure SDK Library, 
 * and performing 
 */
@Component
public class BlobStorageClientAzure implements BlobStorageClient {

  private final BlobStorageConfig blobStorageConfig;
  private final BlobServiceClientBuilder blobServiceClientBuilder;

  public BlobStorageClientAzure(BlobStorageConfig blobStorageConfig,
      BlobServiceClientBuilder blobServiceClientBuilder) {
    this.blobStorageConfig = blobStorageConfig;
    this.blobServiceClientBuilder = blobServiceClientBuilder;
  }

  @Override
  public Mono<ServiceProviderDataResponse> getServiceProviderData() {
    String endpoint = String.format("https://%s.blob.core.windows.net", blobStorageConfig.storageAccountName());

    BlobServiceClient blobServiceClient = blobServiceClientBuilder
        .endpoint(endpoint)
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

    BlobContainerClient containerClient = blobServiceClient
        .getBlobContainerClient(blobStorageConfig.containerName());

    BlobClient blobClient = containerClient
        .getBlobClient(blobStorageConfig.blobName());

    return Mono.just(blobClient.downloadContent().toObject(ServiceProviderDataResponse.class));
  }
}