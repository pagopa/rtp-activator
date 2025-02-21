package it.gov.pagopa.rtp.activator.service.registryfile;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.gov.pagopa.rtp.activator.domain.registryfile.ServiceProvider;
import it.gov.pagopa.rtp.activator.domain.registryfile.ServiceProviderFullData;
import it.gov.pagopa.rtp.activator.domain.registryfile.TechnicalServiceProvider;
import it.gov.pagopa.rtp.activator.integration.blobstorage.BlobStorageClient;
import it.gov.pagopa.rtp.activator.integration.blobstorage.ServiceProviderDataResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class RegistryDataServiceImplTest {

  @Mock
  private BlobStorageClient blobStorageClient;

  private RegistryDataServiceImpl registryDataService;

  @BeforeEach
  void setUp() {
    registryDataService = new RegistryDataServiceImpl(blobStorageClient);
  }

  @Test
  void givenValidResponse_whenGetRegistryData_thenReturnTransformedData() {

    var tsp = new TechnicalServiceProvider("TSP1", "Tech Provider 1", "https://endpoint.com",
        "cert123", null);
    var sp = new ServiceProvider("SP1", "Service Provider 1", "TSP1");
    var serviceProviderDataResponse = new ServiceProviderDataResponse(List.of(tsp), List.of(sp));

    when(blobStorageClient.getServiceProviderData()).thenReturn(
        Mono.just(serviceProviderDataResponse));

    Mono<Map<String, ServiceProviderFullData>> result = registryDataService.getRegistryData();

    StepVerifier.create(result)
        .expectNextMatches(map ->
            map.containsKey("SP1") &&
                map.get("SP1").spName().equals("Service Provider 1") &&
                map.get("SP1").tsp().id().equals("TSP1")
        )
        .verifyComplete();

    verify(blobStorageClient, times(1)).getServiceProviderData();
  }


  @Test
  void givenError_whenGetRegistryData_thenHandleErrorGracefully() {

    when(blobStorageClient.getServiceProviderData()).thenReturn(
        Mono.error(new RuntimeException("Service error")));

    Mono<Map<String, ServiceProviderFullData>> result = registryDataService.getRegistryData();

    StepVerifier.create(result)
        .expectErrorMatches(throwable ->
            throwable instanceof RuntimeException &&
                throwable.getMessage().equals("Service error")
        )
        .verify();

    verify(blobStorageClient, times(1)).getServiceProviderData();
  }


  @Test
  void givenValidData_whenGetRegistryData_thenTransformSuccessfully() {

    ServiceProviderDataResponse mockResponse = new ServiceProviderDataResponse(
        List.of(new TechnicalServiceProvider("TSP1", "Technical Service Provider 1",
            "https://example.com", "123456", null)),
        List.of(new ServiceProvider("SP1", "Service Provider 1", "TSP1"))
    );

    when(blobStorageClient.getServiceProviderData()).thenReturn(Mono.just(mockResponse));

    Mono<Map<String, ServiceProviderFullData>> result = registryDataService.getRegistryData();

    StepVerifier.create(result)
        .expectNextMatches(map ->
            map.containsKey("SP1") &&
                map.get("SP1").spName().equals("Service Provider 1") &&
                map.get("SP1").tsp() != null &&
                map.get("SP1").tsp().id().equals("TSP1")
        )
        .verifyComplete();

    verify(blobStorageClient, times(1)).getServiceProviderData();
  }

}

