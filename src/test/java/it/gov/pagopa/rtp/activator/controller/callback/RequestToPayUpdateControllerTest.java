package it.gov.pagopa.rtp.activator.controller.callback;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import it.gov.pagopa.rtp.activator.domain.errors.IncorrectCertificate;
import it.gov.pagopa.rtp.activator.epcClient.model.AsynchronousSepaRequestToPayResponseDto;
import it.gov.pagopa.rtp.activator.epcClient.model.AsynchronousSepaRequestToPayResponseResourceDto;
import it.gov.pagopa.rtp.activator.epcClient.model.CreditorPaymentActivationRequestStatusReportV07Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.GroupHeader87Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.OrganisationIdentification29EPC25922V30DS04bDto;
import it.gov.pagopa.rtp.activator.epcClient.model.Party38ChoiceIVDto;
import it.gov.pagopa.rtp.activator.epcClient.model.PartyIdentification135Dto;
import it.gov.pagopa.rtp.activator.utils.CheckCertificate;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class RequestToPayUpdateControllerTest {

  @Mock
  private CheckCertificate checkCertificate;

  @InjectMocks
  private RequestToPayUpdateController controller;

  private AsynchronousSepaRequestToPayResponseResourceDto requestBody;
  private final String validCertificateSerialNumber = "123456789ABCDEF";
  private final String invalidCertificateSerialNumber = "INVALID9876543210";
  private final String serviceProviderDebtorId = "ABCDITMMXXX";

  @BeforeEach
  void setUp() {
    requestBody = createMockRequestBody(serviceProviderDebtorId);
  }

  @Test
  void handleRequestToPayUpdateWithValidCertificateShouldReturnOk() {
    when(checkCertificate.verifyRequestCertificate(any(), eq(validCertificateSerialNumber)))
        .thenReturn(Mono.just(requestBody));
    Mono<ResponseEntity<Void>> result = controller.handleRequestToPayUpdate(
        validCertificateSerialNumber, Mono.just(requestBody));

    StepVerifier.create(result)
        .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK)
        .verifyComplete();
  }

  @Test
  void handleRequestToPayUpdateWithInvalidCertificateShouldReturnForbidden() {
    when(checkCertificate.verifyRequestCertificate(any(), eq(invalidCertificateSerialNumber)))
        .thenReturn(Mono.error(new IncorrectCertificate()));

    Mono<ResponseEntity<Void>> result = controller.handleRequestToPayUpdate(
        invalidCertificateSerialNumber, Mono.just(requestBody));

    StepVerifier.create(result)
        .expectNextMatches(response -> response.getStatusCode() == HttpStatus.FORBIDDEN)
        .verifyComplete();
  }

  @Test
  void handleRequestToPayUpdateWithOtherErrorShouldPropagateError() {
    IllegalStateException exception = new IllegalStateException("Test exception");
    when(checkCertificate.verifyRequestCertificate(any(), eq(validCertificateSerialNumber)))
        .thenReturn(Mono.error(exception));

    Mono<ResponseEntity<Void>> result = controller.handleRequestToPayUpdate(
        validCertificateSerialNumber, Mono.just(requestBody));

    StepVerifier.create(result)
        .expectError(IllegalStateException.class)
        .verify();
  }

  @Test
  void handleRequestToPayUpdateWithEmptyRequestShouldReturnBadRequest() {
    Mono<ResponseEntity<Void>> result = controller.handleRequestToPayUpdate(
        validCertificateSerialNumber, Mono.empty());

    StepVerifier.create(result)
        .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST)
        .verifyComplete();
  }

  private AsynchronousSepaRequestToPayResponseResourceDto createMockRequestBody(String serviceProviderDebtorId) {
    var dto = new AsynchronousSepaRequestToPayResponseResourceDto();
    var response = new AsynchronousSepaRequestToPayResponseDto();
    var statusReport = new CreditorPaymentActivationRequestStatusReportV07Dto();
    var groupHeader = new GroupHeader87Dto();
    var initiatingParty = new PartyIdentification135Dto();
    var id = new Party38ChoiceIVDto();
    var orgId = new OrganisationIdentification29EPC25922V30DS04bDto();
    String anyBIC = serviceProviderDebtorId;

    orgId.setAnyBIC(anyBIC);
    id.setOrgId(orgId);
    initiatingParty.setId(id);
    groupHeader.setInitgPty(initiatingParty);
    statusReport.setGrpHdr(groupHeader);
    response.setCdtrPmtActvtnReqStsRpt(statusReport);
    dto.setAsynchronousSepaRequestToPayResponse(response);

    return dto;
  }
}