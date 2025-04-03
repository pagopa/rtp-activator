package it.gov.pagopa.rtp.activator.utils;

import java.util.Optional;
import org.springframework.stereotype.Component;

import it.gov.pagopa.rtp.activator.domain.errors.IncorrectCertificate;
import it.gov.pagopa.rtp.activator.epcClient.model.AsynchronousSepaRequestToPayResponseDto;
import it.gov.pagopa.rtp.activator.epcClient.model.AsynchronousSepaRequestToPayResponseResourceDto;
import it.gov.pagopa.rtp.activator.epcClient.model.CreditorPaymentActivationRequestStatusReportV07Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.GroupHeader87Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.OrganisationIdentification29EPC25922V30DS04bDto;
import it.gov.pagopa.rtp.activator.epcClient.model.Party38ChoiceIVDto;
import it.gov.pagopa.rtp.activator.epcClient.model.PartyIdentification135Dto;
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

    String serviceProviderDebtorId = Optional
        .ofNullable(requestBody.getAsynchronousSepaRequestToPayResponse())
        .map(AsynchronousSepaRequestToPayResponseDto::getCdtrPmtActvtnReqStsRpt)
        .map(CreditorPaymentActivationRequestStatusReportV07Dto::getGrpHdr).map(GroupHeader87Dto::getInitgPty)
        .map(PartyIdentification135Dto::getId).map(Party38ChoiceIVDto::getOrgId)
        .map(OrganisationIdentification29EPC25922V30DS04bDto::getAnyBIC)
        .orElseThrow(() -> new IllegalStateException("AnyBIC is null or empty"));

    return registryDataService.getRegistryData()
        .flatMap(data -> {

          if (!data.containsKey(serviceProviderDebtorId)) {
            return Mono.error(new IllegalStateException(
                "No service provider found for creditor: " + serviceProviderDebtorId));
          }
          return Mono.just(data.get(serviceProviderDebtorId));
        })
        .flatMap(provider -> {
          String certificateServiceNumberRegistry = provider.tsp().certificateSerialNumber();
          if (certificateServiceNumberRegistry.equals(certificateSerialNumber)) {
              log.info("Certificate verified successfully. Serial Number: {}", certificateSerialNumber);
              return Mono.just(requestBody);
          }
          log.warn("Certificate mismatch: expected {}, received {}", certificateServiceNumberRegistry, certificateSerialNumber);
          return Mono.error(new IncorrectCertificate());
        });

  }
}
