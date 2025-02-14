package it.gov.pagopa.rtp.activator.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "blob-storage-config")
public record BlobStorageConfig (String storageAccountName, String containerName, String blobName){
    
}