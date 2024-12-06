package it.gov.pagopa.rtp.activator.service.activation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.gov.pagopa.rtp.activator.domain.errors.PayerAlreadyExists;
import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.domain.payer.PayerID;
import it.gov.pagopa.rtp.activator.repository.activation.ActivationDBRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivationPayerServiceImplTest {

    @Mock
    private ActivationDBRepository activationDBRepository;

    @InjectMocks
    private ActivationPayerServiceImpl activationPayerService;

    private Payer payer;

    @BeforeEach
    void setUp() {
        payer = new Payer(PayerID.createNew(), "RTP_SP_ID", "FISCAL_CODE", Instant.now());

    }

    @Test
    void testActivatePayerSuccessful() {
        when(activationDBRepository.findByFiscalCode("FISCAL_CODE")).thenReturn(Mono.empty());
        when(activationDBRepository.save(any())).thenReturn(Mono.just(payer));

        Mono<Payer> result = activationPayerService.activatePayer("RTP_SP_ID", "FISCAL_CODE");

        StepVerifier.create(result)
            .expectNext(payer)
            .verifyComplete();
    }

    @Test
    void testActivatePayerAlreadyExists() {
        when(activationDBRepository.findByFiscalCode("FISCAL_CODE")).thenReturn(Mono.just(payer));

        Mono<Payer> result = activationPayerService.activatePayer("RTP_SP_ID", "FISCAL_CODE");

        StepVerifier.create(result)
            .expectError(PayerAlreadyExists.class)
            .verify();
    }
}
