package it.gov.pagopa.rtp.activator.utils;

import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.rtp.activator.domain.errors.ServiceProviderNotFoundException;
import java.util.HashMap;
import java.util.Map;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.gov.pagopa.rtp.activator.domain.errors.IncorrectCertificate;

import it.gov.pagopa.rtp.activator.domain.registryfile.ServiceProviderFullData;
import it.gov.pagopa.rtp.activator.domain.registryfile.TechnicalServiceProvider;
import it.gov.pagopa.rtp.activator.service.registryfile.RegistryDataService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CertificateCheckerTest {

  @Mock
  private RegistryDataService registryDataService;

  @InjectMocks
  private CertificateChecker certificateChecker;

  private JsonNode requestBody;
  private final String serviceProviderDebtorId = "ABCDITMMXXX";
  private final String validCertificateSerialNumber = "123456789ABCDEF";


  @BeforeEach
  void setUp() {
    requestBody = createMockRequestBody(serviceProviderDebtorId);

    Map<String, ServiceProviderFullData> registryDataMap = new HashMap<>();
    TechnicalServiceProvider tsp = new TechnicalServiceProvider("fakeTSPId", "fakeTSPName",
        "serviceProviderDebtorId", validCertificateSerialNumber, null, true);
    ServiceProviderFullData serviceProviderFullData = new ServiceProviderFullData("fakeServiceProviderId",
        "fakeServiceProvider", tsp);
    registryDataMap.put(serviceProviderDebtorId, serviceProviderFullData);

    when(registryDataService.getRegistryData()).thenReturn(Mono.just(registryDataMap));
  }

  @Test
  void verifyRequestCertificateWithValidCertificateShouldReturnRequest() {
    Mono<JsonNode> result = certificateChecker
        .verifyRequestCertificate(requestBody, validCertificateSerialNumber);

    StepVerifier.create(result)
        .expectNext(requestBody)
        .verifyComplete();
  }

  @Test
  void verifyRequestCertificateWithInvalidCertificateShouldThrowIncorrectCertificate() {
    String invalidCertificateSerialNumber = "INVALID9876543210";
    Mono<JsonNode> result = certificateChecker
        .verifyRequestCertificate(requestBody, invalidCertificateSerialNumber);

    StepVerifier.create(result)
        .expectError(IncorrectCertificate.class)
        .verify();
  }

  @Test
  void verifyRequestCertificateWithNonExistentServiceProviderShouldThrowServiceProviderNotFoundException() {
    Map<String, ServiceProviderFullData> registryDataMap = new HashMap<>();
    TechnicalServiceProvider tsp = new TechnicalServiceProvider("otherTSPId", "otherTSPName",
        "otherServiceProviderDebtorId", "otherCertSerialNumber", null, true);
    ServiceProviderFullData serviceProviderFullData = new ServiceProviderFullData("otherServiceProviderId",
        "otherServiceProvider", tsp);

    // Add with a different key than what will be searched for
    String differentBIC = "DIFFERENTBIC";
    registryDataMap.put(differentBIC, serviceProviderFullData);

    when(registryDataService.getRegistryData()).thenReturn(Mono.just(registryDataMap));

    Mono<JsonNode> result = certificateChecker
        .verifyRequestCertificate(requestBody, validCertificateSerialNumber);

    StepVerifier.create(result)
        .expectErrorMatches(throwable -> throwable instanceof ServiceProviderNotFoundException &&
            throwable.getMessage().contains("No service provider found for creditor: " + serviceProviderDebtorId))
        .verify();
  }

  private JsonNode createMockRequestBody(String serviceProviderDebtorId) {
    final var baseJson = """
        {
            "resourceId": "TestRtpMessageJZixUlWE3uYcb4k3lF4",
            "AsynchronousSepaRequestToPayResponse": {
                "resourceId": "TestRtpMessageJZixUlWE3uYcb4k3lF4",
                "Document": {
                    "CdtrPmtActvtnReqStsRpt": {
                        "GrpHdr": {
                            "MsgId": "6588c58bcba84b0382422d45e5d04257",
                            "CreDtTm": "2025-03-27T14:10:16.972736305Z",
                            "InitgPty": {
                                "Id": {
                                    "OrgId": {
                                        "AnyBIC": "%s"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        """;

    return Optional.of(serviceProviderDebtorId)
        .map(spId -> String.format(baseJson, spId))
        .map(json -> {
          try {
            return new ObjectMapper()
                .readTree(json);

          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
        })
        .orElseThrow(() -> new RuntimeException("Couldn't create mock request body."));
  }
}