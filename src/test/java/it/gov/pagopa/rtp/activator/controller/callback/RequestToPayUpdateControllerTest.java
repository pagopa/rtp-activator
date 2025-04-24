package it.gov.pagopa.rtp.activator.controller.callback;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.rtp.activator.domain.errors.ServiceProviderNotFoundException;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.MDC;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import it.gov.pagopa.rtp.activator.domain.errors.IncorrectCertificate;
import it.gov.pagopa.rtp.activator.utils.CertificateChecker;
import it.gov.pagopa.rtp.activator.utils.LoggingUtils;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class RequestToPayUpdateControllerTest {

  @Mock private CertificateChecker certificateChecker;

  private RequestToPayUpdateController controller;

  private MockedStatic<LoggingUtils> loggingUtilsMock;

  private JsonNode requestBody;
  private final String validCertificateSerialNumber = "123456789ABCDEF";

  @BeforeEach
  void setUp() {
    loggingUtilsMock = Mockito.mockStatic(LoggingUtils.class);
    loggingUtilsMock
        .when(() -> LoggingUtils.logAsJson(any(Supplier.class), any(ObjectMapper.class)))
        .thenAnswer(
            invocation -> {
              assert "ABCDITMMXXX".equals(MDC.get("service_provider"));
              assert "XYZDEBTOR123".equals(MDC.get("debtor"));
              return null;
            });

    this.controller = new RequestToPayUpdateController(certificateChecker, new ObjectMapper());

    String serviceProviderDebtorId = "ABCDITMMXXX";
    requestBody = createMockRequestBody(serviceProviderDebtorId);
  }

  @AfterEach
  void tearDown() {
    loggingUtilsMock.close();
    MDC.clear();
  }

  @Test
  void handleRequestToPayUpdateWithValidCertificateShouldReturnOk() {
    when(certificateChecker.verifyRequestCertificate(any(), eq(validCertificateSerialNumber)))
        .thenReturn(Mono.just(requestBody));
    Mono<ResponseEntity<Void>> result =
        controller.handleRequestToPayUpdate(validCertificateSerialNumber, Mono.just(requestBody));

    StepVerifier.create(result)
        .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK)
        .verifyComplete();
  }

  @Test
  void handleRequestToPayUpdateWithInvalidCertificateShouldReturnForbidden() {
    String invalidCertificateSerialNumber = "INVALID9876543210";
    when(certificateChecker.verifyRequestCertificate(any(), eq(invalidCertificateSerialNumber)))
        .thenReturn(Mono.error(new IncorrectCertificate()));

    Mono<ResponseEntity<Void>> result =
        controller.handleRequestToPayUpdate(invalidCertificateSerialNumber, Mono.just(requestBody));

    StepVerifier.create(result)
        .expectNextMatches(response -> response.getStatusCode() == HttpStatus.FORBIDDEN)
        .verifyComplete();
  }

  @Test
  void handleRequestToPayUpdateWithNonExistingSpIdShouldReturnBadRequest() {
    final var exception = new ServiceProviderNotFoundException("Test exception");
    when(certificateChecker.verifyRequestCertificate(any(), eq(validCertificateSerialNumber)))
        .thenReturn(Mono.error(exception));

    final var result =
        controller.handleRequestToPayUpdate(validCertificateSerialNumber, Mono.just(requestBody));

    StepVerifier.create(result)
        .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST)
        .verifyComplete();
  }

  @Test
  void handleRequestToPayUpdateWithOtherErrorShouldPropagateError() {
    IllegalStateException exception = new IllegalStateException("Test exception");
    when(certificateChecker.verifyRequestCertificate(any(), eq(validCertificateSerialNumber)))
        .thenReturn(Mono.error(exception));

    Mono<ResponseEntity<Void>> result =
        controller.handleRequestToPayUpdate(validCertificateSerialNumber, Mono.just(requestBody));

    StepVerifier.create(result).expectError(IllegalStateException.class).verify();
  }

  @Test
  void handleRequestToPayUpdateWithEmptyRequestShouldReturnBadRequest() {
    Mono<ResponseEntity<Void>> result =
        controller.handleRequestToPayUpdate(validCertificateSerialNumber, Mono.empty());

    StepVerifier.create(result)
        .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST)
        .verifyComplete();
  }

  private JsonNode createMockRequestBody(String serviceProviderDebtorId) {
    final var baseJson =
        """
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
        .map(
            json -> {
              try {
                return new ObjectMapper().readTree(json);

              } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
              }
            })
        .orElseThrow(() -> new RuntimeException("Couldn't create mock request body."));
  }
}
