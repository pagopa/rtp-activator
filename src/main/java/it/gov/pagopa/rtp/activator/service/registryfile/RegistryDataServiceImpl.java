package it.gov.pagopa.rtp.activator.service.registryfile;

import it.gov.pagopa.rtp.activator.domain.registryfile.ServiceProvider;
import it.gov.pagopa.rtp.activator.domain.registryfile.ServiceProviderFullData;
import it.gov.pagopa.rtp.activator.domain.registryfile.TechnicalServiceProvider;
import it.gov.pagopa.rtp.activator.integration.blobstorage.BlobStorageClient;
import it.gov.pagopa.rtp.activator.integration.blobstorage.ServiceProviderDataResponse;
import it.gov.pagopa.rtp.activator.utils.ExceptionUtils;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflection;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service("registryDataService")
@RegisterReflection(classes = {ServiceProviderFullData.class,})
@Slf4j
public class RegistryDataServiceImpl implements RegistryDataService {

  private final BlobStorageClient blobStorageClient;


  public RegistryDataServiceImpl(
      @NonNull final BlobStorageClient blobStorageClient) {

    this.blobStorageClient = Objects.requireNonNull(
        blobStorageClient, "Blob storage client cannot be null");
  }


  @NonNull
  @Cacheable("registry-data")
  public Mono<Map<String, ServiceProviderFullData>> getRegistryData() {
    final var registryRawData = blobStorageClient.getServiceProviderData()
        .doFirst(() -> log.info("Starting getServiceProviderData"))
        .onErrorMap(ExceptionUtils::gracefullyHandleError)
        .doOnNext(rawData -> log.debug("Successfully retrieved registry raw data"))
        .doOnError(error -> log.error("Error retrieving registry data: {}", error.getMessage()));

    return registryRawData.flatMap(this::transformRegistryFileData)
        .onErrorMap(ExceptionUtils::gracefullyHandleError)
        .doOnSuccess(data -> log.info("Successfully transformed registry data"))
        .doOnError(error -> log.error("Error retrieving registry data: {}", error.getMessage(), error));
  }


  @NonNull
  private Mono<Map<String, ServiceProviderFullData>> transformRegistryFileData(
      @NonNull final ServiceProviderDataResponse serviceProviderDataResponse) {

    return Mono.just(serviceProviderDataResponse)
        .doOnNext(rawData -> log.debug("Transforming registry data"))
        .flatMap(data -> {
          final var technicalServiceProviderMap = Flux.fromIterable(data.tsps())
              .collectMap(TechnicalServiceProvider::id);

          return technicalServiceProviderMap.flatMap(
              tspMap ->
                  Flux.fromIterable(data.sps())
                      .collectMap(
                          ServiceProvider::id,
                          sp -> new ServiceProviderFullData(
                              sp.id(),
                              sp.name(),
                              tspMap.get(sp.tspId())
                          )
                      ));
        });
  }

}
