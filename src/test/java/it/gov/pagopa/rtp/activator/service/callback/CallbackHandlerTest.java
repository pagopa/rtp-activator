package it.gov.pagopa.rtp.activator.service.callback;

import com.fasterxml.jackson.databind.JsonNode;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import it.gov.pagopa.rtp.activator.domain.rtp.*;
import it.gov.pagopa.rtp.activator.service.rtp.RtpStatusUpdater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CallbackHandlerTest {

    @Mock
    private RtpRepository rtpRepository;
    @Mock
    private RtpStatusUpdater rtpStatusUpdater;
    @Mock
    private ServiceProviderConfig serviceProviderConfig;
    @Mock
    private CallbackFieldsExtractor callbackFieldsExtractor;

    @InjectMocks
    private CallbackHandler callbackHandler;

    @Mock
    private ServiceProviderConfig.Send sendConfig;
    @Mock
    private ServiceProviderConfig.Send.Retry retryConfig;

    private final ResourceID resourceID = new ResourceID(UUID.randomUUID());
    private final Rtp rtp = Rtp.builder().resourceID(resourceID).status(RtpStatus.CREATED).build();

    @BeforeEach
    void setup() {
        lenient().when(serviceProviderConfig.send()).thenReturn(sendConfig);
        lenient().when(sendConfig.retry()).thenReturn(retryConfig);
    }

    @Test
    void givenValidACCPStatus_whenHandle_thenAcceptTriggeredAndSaved() {
        JsonNode request = mock(JsonNode.class);

        when(callbackFieldsExtractor.extractTransactionStatusSend(request))
                .thenReturn(Flux.just(TransactionStatus.ACCP));
        when(callbackFieldsExtractor.extractResourceIDSend(request))
                .thenReturn(Mono.just(resourceID));
        when(rtpRepository.findById(resourceID))
                .thenReturn(Mono.just(rtp));
        when(rtpStatusUpdater.triggerAcceptRtp(rtp))
                .thenReturn(Mono.just(rtp));
        when(rtpRepository.save(any(Rtp.class)))
                .thenReturn(Mono.just(rtp));

        StepVerifier.create(callbackHandler.handle(request))
                .expectNext(request)
                .verifyComplete();

        verify(rtpStatusUpdater).triggerAcceptRtp(rtp);
        verify(rtpRepository).save(rtp);
    }

    @Test
    void givenValidRJCTStatus_whenHandle_thenRejectTriggeredAndSaved() {
        JsonNode request = mock(JsonNode.class);

        when(callbackFieldsExtractor.extractTransactionStatusSend(request))
                .thenReturn(Flux.just(TransactionStatus.RJCT));
        when(callbackFieldsExtractor.extractResourceIDSend(request))
                .thenReturn(Mono.just(resourceID));
        when(rtpRepository.findById(resourceID))
                .thenReturn(Mono.just(rtp));
        when(rtpStatusUpdater.triggerRejectRtp(rtp))
                .thenReturn(Mono.just(rtp));
        when(rtpRepository.save(any(Rtp.class)))
                .thenReturn(Mono.just(rtp));

        StepVerifier.create(callbackHandler.handle(request))
                .expectNext(request)
                .verifyComplete();

        verify(rtpStatusUpdater).triggerRejectRtp(rtp);
    }

    @Test
    void givenValidERRORStatus_whenHandle_thenErrorTriggeredAndSaved() {
        JsonNode request = mock(JsonNode.class);

        when(callbackFieldsExtractor.extractTransactionStatusSend(request))
                .thenReturn(Flux.just(TransactionStatus.ERROR));
        when(callbackFieldsExtractor.extractResourceIDSend(request))
                .thenReturn(Mono.just(resourceID));
        when(rtpRepository.findById(resourceID))
                .thenReturn(Mono.just(rtp));
        when(rtpStatusUpdater.triggerErrorSendRtp(rtp))
                .thenReturn(Mono.just(rtp));
        when(rtpRepository.save(any(Rtp.class)))
                .thenReturn(Mono.just(rtp));

        StepVerifier.create(callbackHandler.handle(request))
                .expectNext(request)
                .verifyComplete();

        verify(rtpStatusUpdater).triggerErrorSendRtp(rtp);
    }

    @Test
    void givenInvalidTransactionStatus_whenHandle_thenIllegalStateExceptionThrown() {
        JsonNode request = mock(JsonNode.class);

        when(callbackFieldsExtractor.extractTransactionStatusSend(request))
                .thenReturn(Flux.just(TransactionStatus.ACTC));
        when(callbackFieldsExtractor.extractResourceIDSend(request))
                .thenReturn(Mono.just(resourceID));
        when(rtpRepository.findById(resourceID))
                .thenReturn(Mono.just(rtp));

        StepVerifier.create(callbackHandler.handle(request))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void givenMissingResourceId_whenHandle_thenIllegalArgumentExceptionThrown() {
        JsonNode request = mock(JsonNode.class);

        when(callbackFieldsExtractor.extractResourceIDSend(request))
                .thenReturn(Mono.error(new IllegalArgumentException("Missing field")));

        StepVerifier.create(callbackHandler.handle(request))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void givenTransactionStatusExtractionFails_whenHandle_thenErrorThrown() {
        JsonNode request = mock(JsonNode.class);

        when(callbackFieldsExtractor.extractResourceIDSend(request))
                .thenReturn(Mono.just(resourceID));
        when(rtpRepository.findById(resourceID))
                .thenReturn(Mono.just(rtp));
        when(callbackFieldsExtractor.extractTransactionStatusSend(request))
                .thenReturn(Flux.error(new IllegalArgumentException("Malformed transactionStatus")));

        StepVerifier.create(callbackHandler.handle(request))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void givenErrorInSave_whenHandle_thenErrorThrown() {
        JsonNode request = mock(JsonNode.class);

        when(callbackFieldsExtractor.extractTransactionStatusSend(request))
                .thenReturn(Flux.just(TransactionStatus.ACCP));
        when(callbackFieldsExtractor.extractResourceIDSend(request))
                .thenReturn(Mono.just(resourceID));
        when(rtpRepository.findById(resourceID))
                .thenReturn(Mono.just(rtp));
        when(rtpStatusUpdater.triggerAcceptRtp(rtp))
                .thenReturn(Mono.just(rtp));
        when(rtpRepository.save(any()))
                .thenReturn(Mono.error(new RuntimeException("DB down")));

        StepVerifier.create(callbackHandler.handle(request))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void givenMultipleTransactionStatuses_whenHandle_thenAllProcessedSequentially() {
        JsonNode request = mock(JsonNode.class);

        when(callbackFieldsExtractor.extractTransactionStatusSend(request))
                .thenReturn(Flux.just(TransactionStatus.ACCP, TransactionStatus.RJCT));
        when(callbackFieldsExtractor.extractResourceIDSend(request))
                .thenReturn(Mono.just(resourceID));
        when(rtpRepository.findById(resourceID))
                .thenReturn(Mono.just(rtp));
        when(rtpStatusUpdater.triggerAcceptRtp(rtp))
                .thenReturn(Mono.just(rtp));
        when(rtpStatusUpdater.triggerRejectRtp(rtp))
                .thenReturn(Mono.just(rtp));
        when(rtpRepository.save(any()))
                .thenReturn(Mono.just(rtp));

        StepVerifier.create(callbackHandler.handle(request))
                .expectNext(request)
                .verifyComplete();

        verify(rtpStatusUpdater).triggerAcceptRtp(rtp);
        verify(rtpStatusUpdater).triggerRejectRtp(rtp);
        verify(rtpRepository, times(2)).save(rtp);
    }

}
