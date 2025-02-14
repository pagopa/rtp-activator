package it.gov.pagopa.rtp.activator.service.rtp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import it.gov.pagopa.rtp.activator.activateClient.api.ReadApi;
import it.gov.pagopa.rtp.activator.activateClient.model.ActivationDto;
import it.gov.pagopa.rtp.activator.activateClient.model.PayerDto;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig.Activation;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig.Send;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig.Send.Retry;
import it.gov.pagopa.rtp.activator.domain.errors.MessageBadFormed;
import it.gov.pagopa.rtp.activator.domain.errors.PayerNotActivatedException;
import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpRepository;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import it.gov.pagopa.rtp.activator.epcClient.api.DefaultApi;
import it.gov.pagopa.rtp.activator.epcClient.model.SepaRequestToPayRequestResourceDto;
import it.gov.pagopa.rtp.activator.epcClient.model.SynchronousSepaRequestToPayCreationResponseDto;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        new Send("v1", new Retry(3, 100, 0.75)));
    @Mock
    private RtpRepository rtpRepository;
    @Mock
    private DefaultApi defaultApi;

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
                serviceProviderConfig, rtpRepository, defaultApi);
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
        when(rtpRepository.save(any()))
                .thenReturn(Mono.just(expectedRtp));
        when(defaultApi.postRequestToPayRequests(any(), any(), any()))
            .thenReturn(Mono.just(new SynchronousSepaRequestToPayCreationResponseDto()));

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
        verify(sepaRequestToPayMapper, times(2)).toEpcRequestToPay(any(Rtp.class));
        verify(readApi, times(1)).findActivationByPayerId(any(), any(), any());
        verify(defaultApi, times(1)).postRequestToPayRequests(any(), any(), any());
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
            .thenReturn(Mono.error(new WebClientResponseException(500, "Internal Server Error", null, null, null)));

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

        when(readApi.findActivationByPayerId(any(), any(), any()))
            .thenReturn(Mono.just(fakeActivationDto));
        when(defaultApi.postRequestToPayRequests(any(), any(), any()))
            .thenReturn(Mono.error(new WebClientResponseException(500, "Internal Server Error", null, null, null)));
        when(rtpRepository.save(any()))
            .thenReturn(Mono.just(expectedRtp));

        Mono<Rtp> result = sendRTPService.send(inputRtp);

        StepVerifier.create(result)
            .expectError(UnsupportedOperationException.class)
            .verify();

        verify(sepaRequestToPayMapper, times(2)).toEpcRequestToPay(any(Rtp.class));
        verify(readApi, times(1)).findActivationByPayerId(any(), any(), any());
        verify(defaultApi, times(1)).postRequestToPayRequests(any(), any(), any());
        verify(rtpRepository, times(1)).save(any());
    }


    @Test
    void givenRtp_whenSavingFailsOnce_thenRetriesAndSucceeds() {
        final var resourceId = ResourceID.createNew();
        final var savingDateTime = LocalDateTime.now();

        final var sourceRtp = mockRtp(RtpStatus.CREATED, resourceId, savingDateTime);
        final var rtpSent = mockRtp(RtpStatus.SENT, resourceId, savingDateTime);

        when(readApi.findActivationByPayerId(any(), any(), any()))
                .thenReturn(Mono.just(mockActivationDto()));

        /*
         * Mocks the save method.
         * The first then return is due to a prior invocation of the method that is not under retry test.
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


        when(defaultApi.postRequestToPayRequests(any(), any(), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(sendRTPService.send(sourceRtp))
                .expectNext(rtpSent)
                .verifyComplete();

        verify(rtpRepository, times(2)).save(any());
    }


    @Test
    void givenRtp_whenSavingFailsIndefinitely_thenThrows() {
        final var sourceRtp = mockRtp();


        when(readApi.findActivationByPayerId(any(), any(), any()))
                .thenReturn(Mono.just(mockActivationDto()));


        /*
         * Mocks the save method.
         * The first then return is due to a prior invocation of the method that is not under retry test.
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


        when(defaultApi.postRequestToPayRequests(any(), any(), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(sendRTPService.send(sourceRtp))
                .expectError(RuntimeException.class)
                .verify();

        verify(rtpRepository, times(2)).save(any());
    }


    private Rtp mockRtp() {
        return mockRtp(RtpStatus.CREATED, ResourceID.createNew(), LocalDateTime.now());
    }


    private Rtp mockRtp(
            @NonNull final RtpStatus status,
            @NonNull final ResourceID resourceId,
            @NonNull final LocalDateTime savingDateTime
    ) {
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
