package it.gov.pagopa.rtp.activator.service.callback;

import org.springframework.stereotype.Component;

import it.gov.pagopa.rtp.activator.domain.errors.IncorrectCertificate;
import it.gov.pagopa.rtp.activator.service.registryfile.RegistryDataService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class RequestToPayUpdateServiceImpl implements RequestToPayUpdateService {

  private final RegistryDataService registryDataService;

  public RequestToPayUpdateServiceImpl(RegistryDataService registryDataService) {
    this.registryDataService = registryDataService;
  }

  @Override
  public Mono<String> checkCallback(String certificateSerialNumber, String serviceProviderDebtorId) {

    return registryDataService.getRegistryData()
        .map(data -> data.get(serviceProviderDebtorId)).switchIfEmpty(Mono.error(new IllegalStateException(
            "No service provider found for creditor: " + serviceProviderDebtorId)))
        .flatMap(provider -> {
          String certificateServiceNumberRegistry = provider.tsp().certificateSerialNumber();
          log.info("Certificate Serial Number from caller: {} and from registry", certificateSerialNumber,
              certificateServiceNumberRegistry);
          if (certificateSerialNumber == certificateServiceNumberRegistry) {
            return Mono.just(certificateSerialNumber);
          } else {
            return Mono.error(new IncorrectCertificate());
          }
        });

  }

}
