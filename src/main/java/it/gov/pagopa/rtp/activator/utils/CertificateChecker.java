package it.gov.pagopa.rtp.activator.utils;

import java.lang.foreign.Linker.Option;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import it.gov.pagopa.rtp.activator.domain.errors.IncorrectCertificate;
import it.gov.pagopa.rtp.activator.epcClient.model.AsynchronousSepaRequestToPayResponseResourceDto;
import it.gov.pagopa.rtp.activator.service.registryfile.RegistryDataService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class CertificateChecker {

  private final RegistryDataService registryDataService;

  public CertificateChecker(RegistryDataService registryDataService) {
    this.registryDataService = registryDataService;
  }

  public Mono<AsynchronousSepaRequestToPayResponseResourceDto> verifyRequestCertificate(
      AsynchronousSepaRequestToPayResponseResourceDto requestBody, String certificateSerialNumber) {

    Optional<String> serviceProviderDebtorId = Optional.ofNullable(requestBody.getAsynchronousSepaRequestToPayResponse().getCdtrPmtActvtnReqStsRpt()
        .getGrpHdr().getInitgPty().getId().getOrgId().getAnyBIC());
      
    // Check if the optional is empty
    if(!serviceProviderDebtorId.isPresent()){
      return Mono.error(new IllegalStateException("AnyBIC is null or empty"));
    }
    return registryDataService.getRegistryData()
        .flatMap(data -> {
          
          if (!data.containsKey(serviceProviderDebtorId.get())) {
            return Mono.error(new IllegalStateException(
              "No service provider found for creditor: " + serviceProviderDebtorId.get()));
          }
          return Mono.just(data.get(serviceProviderDebtorId.get()));
        })
        .flatMap(provider -> {
          String certificateServiceNumberRegistry = provider.tsp().certificateSerialNumber();
          log.info("Certificate Serial Number from caller: {} and from registry", certificateSerialNumber,
              certificateServiceNumberRegistry);
          if (certificateServiceNumberRegistry.equals(certificateSerialNumber)) {
            return Mono.just(requestBody);
          }
          return Mono.error(new IncorrectCertificate());
        });

  }
}
