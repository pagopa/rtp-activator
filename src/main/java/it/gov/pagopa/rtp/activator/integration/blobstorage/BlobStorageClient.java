package it.gov.pagopa.rtp.activator.integration.blobstorage;

import com.azure.core.util.BinaryData;

import reactor.core.publisher.Mono;

/*
 * Used to access to blob storage account.
 */
public interface BlobStorageClient {
    Mono<BinaryData> getServiceProviderData();
}
