package it.gov.pagopa.rtp.activator.service.rtp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.rtp.activator.activateClient.api.ReadApi;
import it.gov.pagopa.rtp.activator.activateClient.model.ActivationDto;
import it.gov.pagopa.rtp.activator.activateClient.model.PayerDto;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig.Activation;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig.Send;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig.Send.Retry;
import it.gov.pagopa.rtp.activator.domain.errors.MessageBadFormed;
import it.gov.pagopa.rtp.activator.domain.errors.PayerNotActivatedException;
import it.gov.pagopa.rtp.activator.domain.errors.RtpNotFoundException;
import it.gov.pagopa.rtp.activator.domain.rtp.Event;
import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpEvent;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpRepository;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import it.gov.pagopa.rtp.activator.epcClient.model.SepaRequestToPayRequestResourceDto;

import it.gov.pagopa.rtp.activator.service.rtp.handler.SendRtpProcessor;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SendRTPServiceTest {

  @Mock
  private SepaRequestToPayMapper sepaRequestToPayMapper;
  @Mock
  private ReadApi readApi;
  private final ServiceProviderConfig serviceProviderConfig = new ServiceProviderConfig(
      "http://localhost:8080",
      new Activation("http://localhost:8080"),
      new Send("v1", new Retry(3, 100, 0.75), 10000L));
  @Mock
  private RtpRepository rtpRepository;
  @Mock
  private ObjectMapper objectMapper;
  @Mock
  private SendRtpProcessor sendRtpProcessor;

  private SendRTPServiceImpl sendRTPService;

  final String noticeNumber = "12345";
  final BigDecimal amount = new BigDecimal("99999999999");
  final String description = "Payment Description";
  final LocalDate expiryDate = LocalDate.now();
  final String payerId = "payerId";
  final String payeeName = "Payee Name";
  final String payerName = "Payer Name";
  final String payeeId = "payeeId";
  final String rtpSpId = "rtpSpId";
  final String iban = "IT60X0542811101000000123456";
  final String payTrxRef = "ABC/124";
  final String flgConf = "flgConf";
  final String subject = "subject";
  final String activationRtpSpId = "activationRtpSpId";

  Rtp inputRtp;

  @BeforeEach
  void setUp() {
    sendRTPService = new SendRTPServiceImpl(sepaRequestToPayMapper, readApi,
        serviceProviderConfig, rtpRepository, objectMapper,sendRtpProcessor);
    inputRtp = Rtp.builder().noticeNumber(noticeNumber).amount(amount).description(description)
        .expiryDate(expiryDate)
        .payerId(payerId).payeeName(payeeName).payeeId(payeeId)
        .resourceID(ResourceID.createNew())
        .savingDateTime(LocalDateTime.now()).serviceProviderDebtor(rtpSpId)
        .iban(iban).payTrxRef(payTrxRef)
        .flgConf(flgConf)
        .payerName(payerName)
        .subject(subject).build();
  }

  @Test
  void testSend() {
    var fakeActivationDto = mockActivationDto();

    var expectedRtp = mockRtp();

    SepaRequestToPayRequestResourceDto mockSepaRequestToPayRequestResource = new SepaRequestToPayRequestResourceDto()
        .callbackUrl(URI.create("http://callback.url"));

    when(sepaRequestToPayMapper.toEpcRequestToPay(any()))
        .thenReturn(mockSepaRequestToPayRequestResource);
    when(readApi.findActivationByPayerId(any(), any(), any()))
        .thenReturn(Mono.just(fakeActivationDto));
    when(sendRtpProcessor.sendRtpToServiceProviderDebtor(any()))
        .thenReturn(Mono.just(expectedRtp));
    when(rtpRepository.save(any()))
        .thenReturn(Mono.just(expectedRtp));

    Mono<Rtp> result = sendRTPService.send(inputRtp);
    StepVerifier.create(result)
        .expectNextMatches(rtp -> rtp.noticeNumber().equals(expectedRtp.noticeNumber())
            && rtp.amount().equals(expectedRtp.amount())
            && rtp.description().equals(expectedRtp.description())
            && rtp.expiryDate().equals(expectedRtp.expiryDate())
            && rtp.payerId().equals(expectedRtp.payerId())
            && rtp.payerName().equals(expectedRtp.payerName())
            && rtp.payeeName().equals(expectedRtp.payeeName())
            && rtp.payeeId().equals(expectedRtp.payeeId())
            && rtp.serviceProviderDebtor().equals(expectedRtp.serviceProviderDebtor())
            && rtp.iban().equals(expectedRtp.iban())
            && rtp.payTrxRef().equals(expectedRtp.payTrxRef())
            && rtp.flgConf().equals(expectedRtp.flgConf())
            && rtp.status().equals(expectedRtp.status())
            && rtp.subject().equals(expectedRtp.subject()))
        .verifyComplete();
    verify(sepaRequestToPayMapper, times(1)).toEpcRequestToPay(any(Rtp.class));
    verify(readApi, times(1)).findActivationByPayerId(any(), any(), any());
    verify(rtpRepository, times(2)).save(any());
  }

  @Test
  void givenPayerIdNotActivatedWhenSendThenMonoError() {

    when(readApi.findActivationByPayerId(any(), any(), any()))
        .thenReturn(Mono.error(new WebClientResponseException(404, "Not Found", null, null, null)));

    Mono<Rtp> result = sendRTPService.send(inputRtp);

    StepVerifier.create(result)
        .expectError(PayerNotActivatedException.class)
        .verify();

    verify(sepaRequestToPayMapper, times(0)).toEpcRequestToPay(any(Rtp.class));
    verify(readApi, times(1)).findActivationByPayerId(any(), any(), any());
  }

  @Test
  void givenPayerIdBadFormedWhenSendThenMonoError() {
    when(readApi.findActivationByPayerId(any(), any(), any()))
        .thenReturn(Mono.error(new WebClientResponseException(400, "Bad Request", null,
            "{}".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8)));

    Mono<Rtp> result = sendRTPService.send(inputRtp);

    StepVerifier.create(result)
        .expectError(MessageBadFormed.class)
        .verify();

    verify(sepaRequestToPayMapper, times(0)).toEpcRequestToPay(any(Rtp.class));
    verify(readApi, times(1)).findActivationByPayerId(any(), any(), any());
  }

  @Test
  void givenInternalErrorWhenSendThenMonoError() {

    when(readApi.findActivationByPayerId(any(), any(), any()))
        .thenReturn(Mono.error(
            new WebClientResponseException(500, "Internal Server Error", null, null, null)));

    Mono<Rtp> result = sendRTPService.send(inputRtp);

    StepVerifier.create(result)
        .expectError(RuntimeException.class)
        .verify();

    verify(sepaRequestToPayMapper, times(0)).toEpcRequestToPay(any(Rtp.class));
    verify(readApi, times(1)).findActivationByPayerId(any(), any(), any());
  }

  @Test
  void givenInternalErrorOnExternalSendWhenSendThenPropagateMonoError() {
    var fakeActivationDto = mockActivationDto();
    var expectedRtp = mockRtp();

    when(readApi.findActivationByPayerId(any(), any(), any())).thenReturn(Mono.just(fakeActivationDto));

    when(sendRtpProcessor.sendRtpToServiceProviderDebtor(any()))
        .thenReturn(Mono.error(new WebClientResponseException(500, "Internal Server Error", null, null, null)));

    when(rtpRepository.save(any()))
        .thenReturn(Mono.just(expectedRtp));

    Mono<Rtp> result = sendRTPService.send(inputRtp);

    StepVerifier.create(result)
        .expectError(UnsupportedOperationException.class)
        .verify();

    verify(rtpRepository, times(1)).save(any());
  }

  @Test
  void givenRtp_whenSavingFailsOnce_thenRetriesAndSucceeds() {
    final var resourceId = ResourceID.createNew();
    final var savingDateTime = LocalDateTime.now();

    final var sourceRtp = mockRtp(RtpStatus.CREATED, resourceId, savingDateTime);
    final var rtpSent = mockRtp(RtpStatus.SENT, resourceId, savingDateTime)
        .withEvents(List.of(
            Event.builder()
                .timestamp(Instant.now())
                .precStatus(RtpStatus.CREATED)
                .triggerEvent(RtpEvent.SEND_RTP)
                .build()
        ));

    when(readApi.findActivationByPayerId(any(), any(), any()))
        .thenReturn(Mono.just(mockActivationDto()));

    when(sendRtpProcessor.sendRtpToServiceProviderDebtor(any()))
        .thenReturn(Mono.just(sourceRtp));

    /*
     * Mocks the save method.
     * The first then return is due to a prior invocation of the method that is not
     * under retry test.
     * Subsequent returns are actually testing retry logic.
     */
    final var saveAttempts = new AtomicInteger();
    when(rtpRepository.save(any()))
        .thenAnswer(invocation -> {
          if (saveAttempts.getAndIncrement() == 2) {
            return Mono.error(new RuntimeException("Simulated DB failure"));
          }
          return Mono.just(invocation.getArgument(0));
        });

    StepVerifier.create(sendRTPService.send(sourceRtp))
        .assertNext(actualRtp -> {
          assertEquals(rtpSent.noticeNumber(), actualRtp.noticeNumber());
          assertEquals(rtpSent.amount(), actualRtp.amount());
          assertEquals(rtpSent.description(), actualRtp.description());
          assertEquals(rtpSent.expiryDate(), actualRtp.expiryDate());
          assertEquals(rtpSent.payerId(), actualRtp.payerId());
          assertEquals(rtpSent.payerName(), actualRtp.payerName());
          assertEquals(rtpSent.payeeName(), actualRtp.payeeName());
          assertEquals(rtpSent.payeeId(), actualRtp.payeeId());
          assertEquals(rtpSent.resourceID(), actualRtp.resourceID());
          assertEquals(rtpSent.subject(), actualRtp.subject());
          assertEquals(rtpSent.savingDateTime(), actualRtp.savingDateTime());
          assertEquals(rtpSent.serviceProviderDebtor(), actualRtp.serviceProviderDebtor());
          assertEquals(rtpSent.iban(), actualRtp.iban());
          assertEquals(rtpSent.payTrxRef(), actualRtp.payTrxRef());
          assertEquals(rtpSent.flgConf(), actualRtp.flgConf());
          assertEquals(rtpSent.status(), actualRtp.status());
          assertEquals(rtpSent.serviceProviderCreditor(), actualRtp.serviceProviderCreditor());

          /*
          The assertion on the timestamp field is not present as is not strictly relevant to check whether
          the expected and actual ones match
           */
          assertEquals(rtpSent.events().size(), actualRtp.events().size());
          for (int i = 0; i < rtpSent.events().size(); i++) {
            final var expectedEvent = rtpSent.events().get(i);
            final var actualEvent = actualRtp.events().get(i);
            assertEquals(expectedEvent.precStatus(), actualEvent.precStatus());
            assertEquals(expectedEvent.triggerEvent(), actualEvent.triggerEvent());
          }
        })
        .verifyComplete();


    verify(rtpRepository, times(2)).save(any());
  }

  @Test
  void givenRtp_whenSavingFailsIndefinitely_thenThrows() {
    final var sourceRtp = mockRtp();

    when(readApi.findActivationByPayerId(any(), any(), any()))
        .thenReturn(Mono.just(mockActivationDto()));

    when(sendRtpProcessor.sendRtpToServiceProviderDebtor(any()))
        .thenReturn(Mono.just(sourceRtp));

    /*
     * Mocks the save method.
     * The first then return is due to a prior invocation of the method that is not
     * under retry test.
     * Subsequent returns are actually testing retry logic.
     */

    final var firstSaveAttempt = new AtomicBoolean(true);
    when(rtpRepository.save(any()))
        .thenAnswer(invocation -> {
          if (firstSaveAttempt.getAndSet(false)) {
            return Mono.just(invocation.getArgument(0));
          }
          return Mono.error(new RuntimeException("Simulated DB failure"));
        });

    StepVerifier.create(sendRTPService.send(sourceRtp))
        .expectError(RuntimeException.class)
        .verify();

    verify(rtpRepository, times(2)).save(any());
  }


  @Test
  void givenExistingCreatedRtp_whenCancelRtp_thenShouldSetCancelledStatusAndSave() {
    final var rtpId = ResourceID.createNew();
    final var createdRtp = mockRtp(RtpStatus.CREATED, rtpId, LocalDateTime.now());

    when(rtpRepository.findById(rtpId)).thenReturn(Mono.just(createdRtp));
    when(sendRtpProcessor.sendRtpCancellationToServiceProviderDebtor(createdRtp))
        .thenReturn(Mono.just(createdRtp));
    when(rtpRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

    final var result = sendRTPService.cancelRtp(rtpId);

    StepVerifier.create(result)
        .assertNext(rtp -> {
          assertEquals(RtpStatus.CANCELLED, rtp.status());
          assertEquals(rtpId, rtp.resourceID());
        })
        .verifyComplete();

    verify(rtpRepository).findById(rtpId);
    verify(sendRtpProcessor).sendRtpCancellationToServiceProviderDebtor(createdRtp);
    verify(rtpRepository).save(any(Rtp.class));
  }

  @Test
  void givenNonExistingRtp_whenCancelRtp_thenShouldThrowRtpNotFoundException() {
    final var rtpId = ResourceID.createNew();

    when(rtpRepository.findById(rtpId)).thenReturn(Mono.empty());

    final var result = sendRTPService.cancelRtp(rtpId);

    StepVerifier.create(result)
        .expectError(RtpNotFoundException.class)
        .verify();

    verify(rtpRepository).findById(rtpId);
    verifyNoInteractions(sendRtpProcessor);
    verifyNoMoreInteractions(rtpRepository);
  }


  private Rtp mockRtp() {
    return mockRtp(RtpStatus.CREATED, ResourceID.createNew(), LocalDateTime.now());
  }

  private Rtp mockRtp(
      @NonNull final RtpStatus status,
      @NonNull final ResourceID resourceId,
      @NonNull final LocalDateTime savingDateTime) {
    return Rtp.builder().noticeNumber(noticeNumber).amount(amount).description(description)
        .expiryDate(expiryDate)
        .payerId(payerId).payeeName(payeeName).payeeId(payeeId)
        .payerName(payerName)
        .resourceID(resourceId)
        .savingDateTime(savingDateTime).serviceProviderDebtor(activationRtpSpId)
        .iban(iban).payTrxRef(payTrxRef)
        .status(status)
        .flgConf(flgConf)
        .subject(subject)
        .events(List.of())
        .build();
  }

  private ActivationDto mockActivationDto() {
    var spId = "activationRtpSpId";
    var fiscalCode = "activationFiscalCode";

    var payerDto = new PayerDto();
    payerDto.setRtpSpId(spId);
    payerDto.setFiscalCode(fiscalCode);

    var fakeActivationDto = new ActivationDto();
    fakeActivationDto.setId(UUID.randomUUID());
    fakeActivationDto.setEffectiveActivationDate(LocalDateTime.now());
    fakeActivationDto.setPayer(payerDto);

    return fakeActivationDto;
  }

}