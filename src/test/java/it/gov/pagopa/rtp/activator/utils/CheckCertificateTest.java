package it.gov.pagopa.rtp.activator.utils;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.gov.pagopa.rtp.activator.domain.errors.IncorrectCertificate;

import it.gov.pagopa.rtp.activator.domain.registryfile.ServiceProviderFullData;
import it.gov.pagopa.rtp.activator.domain.registryfile.TechnicalServiceProvider;
import it.gov.pagopa.rtp.activator.epcClient.model.AsynchronousSepaRequestToPayResponseDto;
import it.gov.pagopa.rtp.activator.epcClient.model.AsynchronousSepaRequestToPayResponseResourceDto;
import it.gov.pagopa.rtp.activator.epcClient.model.CreditorPaymentActivationRequestStatusReportV07Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.GroupHeader87Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.OrganisationIdentification29EPC25922V30DS04bDto;
import it.gov.pagopa.rtp.activator.epcClient.model.Party38ChoiceIVDto;
import it.gov.pagopa.rtp.activator.epcClient.model.PartyIdentification135Dto;
import it.gov.pagopa.rtp.activator.service.registryfile.RegistryDataService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CheckCertificateTest {

  @Mock
  private RegistryDataService registryDataService;

  @InjectMocks
  private CheckCertificate checkCertificate;

  private AsynchronousSepaRequestToPayResponseResourceDto requestBody;
  private final String serviceProviderDebtorId = "ABCDITMMXXX";
  private final String validCertificateSerialNumber = "123456789ABCDEF";
  private final String invalidCertificateSerialNumber = "INVALID9876543210";

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
    Mono<AsynchronousSepaRequestToPayResponseResourceDto> result = checkCertificate
        .verifyRequestCertificate(requestBody, validCertificateSerialNumber);

    StepVerifier.create(result)
        .expectNext(requestBody)
        .verifyComplete();
  }

  @Test
  void verifyRequestCertificateWithInvalidCertificateShouldThrowIncorrectCertificate() {
    Mono<AsynchronousSepaRequestToPayResponseResourceDto> result = checkCertificate
        .verifyRequestCertificate(requestBody, invalidCertificateSerialNumber);

    StepVerifier.create(result)
        .expectError(IncorrectCertificate.class)
        .verify();
  }

  @Test
  void verifyRequestCertificateWithNonExistentServiceProviderShouldThrowIllegalStateException() {
    Map<String, ServiceProviderFullData> registryDataMap = new HashMap<>();
    TechnicalServiceProvider tsp = new TechnicalServiceProvider("otherTSPId", "otherTSPName",
        "otherServiceProviderDebtorId", "otherCertSerialNumber", null, true);
    ServiceProviderFullData serviceProviderFullData = new ServiceProviderFullData("otherServiceProviderId",
        "otherServiceProvider", tsp);

    // Add with a different key than what will be searched for
    String differentBIC = "DIFFERENTBIC";
    registryDataMap.put(differentBIC, serviceProviderFullData);

    when(registryDataService.getRegistryData()).thenReturn(Mono.just(registryDataMap));

    Mono<AsynchronousSepaRequestToPayResponseResourceDto> result = checkCertificate
        .verifyRequestCertificate(requestBody, validCertificateSerialNumber);

    StepVerifier.create(result)
        .expectErrorMatches(throwable -> throwable instanceof IllegalStateException &&
            throwable.getMessage().contains("No service provider found for creditor: " + serviceProviderDebtorId))
        .verify();
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