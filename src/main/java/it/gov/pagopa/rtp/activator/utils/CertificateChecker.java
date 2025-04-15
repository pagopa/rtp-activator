package it.gov.pagopa.rtp.activator.utils;

import static it.gov.pagopa.rtp.activator.utils.LoggingUtils.sanitize;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import it.gov.pagopa.rtp.activator.domain.errors.IncorrectCertificate;
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

  public Mono<JsonNode> verifyRequestCertificate(
      JsonNode requestBody, String certificateSerialNumber) {

    final var serviceProviderDebtorId = Optional.of(requestBody)
        .map(node -> node.path("AsynchronousSepaRequestToPayResponse"))
        .map(node -> {
          final var creditorPaymentActivationRequestStatusReportLabel = "CdtrPmtActvtnReqStsRpt";

          return Optional.of(node)
              .filter(innerNode -> !innerNode.has(creditorPaymentActivationRequestStatusReportLabel))
              .map(innerNode -> innerNode.path("Document"))
              .map(innerNode -> innerNode.path(creditorPaymentActivationRequestStatusReportLabel))
              .orElseGet(() -> node.path(creditorPaymentActivationRequestStatusReportLabel));
        })
        .map(node -> node.path("GrpHdr"))
        .map(node -> node.path("InitgPty"))
        .map(node -> node.path("Id"))
        .map(node -> node.path("OrgId"))
        .map(node -> node.path("AnyBIC"))
        .map(JsonNode::asText)
        .map(StringUtils::trimToNull)
        .orElseThrow(() -> new IllegalArgumentException("Couldn't parse Service Provider of Debtor id."));

    return registryDataService.getRegistryData()
        .flatMap(data -> Mono.justOrEmpty(data.get(serviceProviderDebtorId))
            .switchIfEmpty(Mono.error(new IllegalStateException(
                "No service provider found for creditor: " + serviceProviderDebtorId))))
        .flatMap(provider -> {
          String certificateServiceNumberRegistry = provider.tsp().certificateSerialNumber();
          if (certificateServiceNumberRegistry.equals(certificateSerialNumber)) {
            log.info("Certificate verified successfully. Serial Number: {}",
                certificateServiceNumberRegistry);
            return Mono.just(requestBody);
          }
          log.warn("Certificate mismatch: expected {}, received {}",
              certificateServiceNumberRegistry, sanitize(certificateSerialNumber));
          return Mono.error(new IncorrectCertificate());
        });

  }
}
