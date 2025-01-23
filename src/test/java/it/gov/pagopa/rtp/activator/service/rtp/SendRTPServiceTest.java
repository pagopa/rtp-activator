package it.gov.pagopa.rtp.activator.service.rtp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.gov.pagopa.rtp.activator.activateClient.api.ReadApi;
import it.gov.pagopa.rtp.activator.activateClient.model.ActivationDto;
import it.gov.pagopa.rtp.activator.activateClient.model.PayerDto;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SendRTPServiceTest {

    @Mock
    private SepaRequestToPayMapper sepaRequestToPayMapper;
    @Mock
    private ReadApi readApi;
    private final ServiceProviderConfig serviceProviderConfig = new ServiceProviderConfig("v1", "http://localhost:8080");
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
    final String payeeId = "payeeId";
    final String rtpSpId = "rtpSpId";
    final String iban = "IT60X0542811101000000123456";
    final String payTrxRef = "ABC/124";
    final String flgConf = "flgConf";
    final String subject = "subject";

    Rtp inputRtp;

    @BeforeEach
    void setUp() {
        sendRTPService = new SendRTPServiceImpl(sepaRequestToPayMapper, readApi,
                serviceProviderConfig, rtpRepository, defaultApi);
        inputRtp = Rtp.builder().noticeNumber(noticeNumber).amount(amount).description(description)
            .expiryDate(expiryDate)
            .payerId(payerId).payeeName(payeeName).payeeId(payeeId)
            .resourceID(ResourceID.createNew())
            .savingDateTime(LocalDateTime.now()).rtpSpId(rtpSpId)
            .iban(iban).payTrxRef(payTrxRef)
            .flgConf(flgConf)
            .subject(subject).build();
    }

    @Test
    void testSend() {

        var activationRtpSpId = "activationRtpSpId";
        var activationFiscalCode = "activationFiscalCode";

        var fakeActivationDto = new ActivationDto();
        fakeActivationDto.setId(UUID.randomUUID());
        fakeActivationDto.setEffectiveActivationDate(LocalDateTime.now());
        var payerDto = new PayerDto();
        payerDto.setRtpSpId(activationRtpSpId);
        payerDto.setFiscalCode(activationFiscalCode);
        fakeActivationDto.setPayer(payerDto);

        var expectedRtp = Rtp.builder().noticeNumber(noticeNumber).amount(amount).description(description)
            .expiryDate(expiryDate)
            .payerId(payerId).payeeName(payeeName).payeeId(payeeId)
            .resourceID(ResourceID.createNew())
            .savingDateTime(LocalDateTime.now()).rtpSpId(activationRtpSpId)
            .iban(iban).payTrxRef(payTrxRef)
            .status(RtpStatus.CREATED)
            .flgConf(flgConf)
            .subject(subject)
            .build();
        SepaRequestToPayRequestResourceDto mockSepaRequestToPayRequestResource = new SepaRequestToPayRequestResourceDto()
            .callbackUrl(URI.create("http://callback.url"));

        when(sepaRequestToPayMapper.toEpcRequestToPay(any(Rtp.class)))
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
                        && rtp.payeeName().equals(expectedRtp.payeeName())
                        && rtp.payeeId().equals(expectedRtp.payeeId())
                        && rtp.rtpSpId().equals(expectedRtp.rtpSpId())
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
        var activationRtpSpId = "activationRtpSpId";
        var activationFiscalCode = "activationFiscalCode";
        var fakeActivationDto = new ActivationDto();
        fakeActivationDto.setId(UUID.randomUUID());
        fakeActivationDto.setEffectiveActivationDate(LocalDateTime.now());
        var payerDto = new PayerDto();
        payerDto.setRtpSpId(activationRtpSpId);
        payerDto.setFiscalCode(activationFiscalCode);
        fakeActivationDto.setPayer(payerDto);
        var expectedRtp = Rtp.builder().resourceID(ResourceID.createNew()).build();

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
}
