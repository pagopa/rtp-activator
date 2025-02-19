package it.gov.pagopa.rtp.activator.integration.blobstorage;

import org.springframework.stereotype.Component;

import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import it.gov.pagopa.rtp.activator.configuration.BlobStorageConfig;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import lombok.extern.slf4j.Slf4j;

/*
 * Interact with the Azure Blob Storage using the Azure SDK Library, 
 * and performing 
 */
@Component
@Slf4j
public class BlobStorageClientAzure implements BlobStorageClient {

  private final BlobStorageConfig blobStorageConfig;
  private final BlobServiceClientBuilder blobServiceClientBuilder;
  private final BlobServiceClient blobServiceClient;

  public BlobStorageClientAzure(BlobStorageConfig blobStorageConfig,
      BlobServiceClientBuilder blobServiceClientBuilder) {
    this.blobStorageConfig = blobStorageConfig;
    this.blobServiceClientBuilder = blobServiceClientBuilder;
    String endpoint = String.format("https://%s.blob.core.windows.net",
        blobStorageConfig.storageAccountName());

    this.blobServiceClient = this.blobServiceClientBuilder
        .endpoint(endpoint)
        .credential(new DefaultAzureCredentialBuilder().managedIdentityClientId(blobStorageConfig.managedIdentity()).build())
        .buildClient();
  }

  @Override
  public Mono<BinaryData> getServiceProviderData() {
    return Mono.fromCallable(() -> {
      log.info("Starting getServiceProviderData for container: {} blob: {}",
          blobStorageConfig.containerName(),
          blobStorageConfig.blobName());

      BlobContainerClient containerClient = blobServiceClient
          .getBlobContainerClient(blobStorageConfig.containerName());
      log.info("Before container client");

      BlobClient blobClient = containerClient
          .getBlobClient(blobStorageConfig.blobName());
      log.info("Before blob client");

      log.info("Before download content");
      return blobClient.downloadContent();
    })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnError(error -> log.error("Error downloading blob: {}", error.getMessage()))
        .doOnSuccess(data -> log.info("Successfully retrieved blob data"));
  }

}