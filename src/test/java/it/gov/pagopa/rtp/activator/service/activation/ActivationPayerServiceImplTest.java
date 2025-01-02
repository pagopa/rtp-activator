package it.gov.pagopa.rtp.activator.service.activation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.gov.pagopa.rtp.activator.domain.errors.PayerAlreadyExists;
import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.domain.payer.ActivationID;
import it.gov.pagopa.rtp.activator.repository.activation.ActivationDBRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivationPayerServiceImplTest {

    @Mock
    private ActivationDBRepository activationDBRepository;

    @InjectMocks
    private ActivationPayerServiceImpl activationPayerService;

    private Payer payer;
    private ActivationID activationID;
    private String rtpSpId;
    private String fiscalCode;

    @BeforeEach
    void setUp() {
        rtpSpId = "testRtpSpId";
        fiscalCode = "TSTFSC12A34B567C";

        activationID = ActivationID.createNew();
        payer = new Payer(activationID, rtpSpId, fiscalCode, Instant.now());
    }

    @Test
    void testActivatePayerSuccessful() {

        when(activationDBRepository.findByFiscalCode(fiscalCode)).thenReturn(Mono.empty());
        when(activationDBRepository.save(any(Payer.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(activationPayerService.activatePayer(rtpSpId, fiscalCode)).expectNextMatches(pay    -> {
            // Verify payer details
            assert pay.rtpSpId().equals(rtpSpId);
            assert pay.fiscalCode().equals(fiscalCode);
            assert pay.activationID() != null;
            assert pay.effectiveActivationDate() != null;
            return true;
        })
                .verifyComplete();

        verify(activationDBRepository).findByFiscalCode(fiscalCode);
        verify(activationDBRepository).save(any(Payer.class));
    }

    @Test
    void testActivatePayerAlreadyExists() {

        when(activationDBRepository.findByFiscalCode(fiscalCode)).thenReturn(Mono.just(payer));

        StepVerifier.create(activationPayerService.activatePayer(rtpSpId, fiscalCode))
                .expectError(PayerAlreadyExists.class)
                .verify();

        verify(activationDBRepository).findByFiscalCode(fiscalCode);
    }

    @Test
    void testFindPayerSuccessful() {
        when(activationDBRepository.findByFiscalCode(fiscalCode)).thenReturn(Mono.just(payer));

        StepVerifier.create(activationPayerService.findPayer(fiscalCode))
                .expectNextMatches(pay -> pay.equals(payer)).verifyComplete();

        verify(activationDBRepository).findByFiscalCode(fiscalCode);

    }

    @Test
    void testFindPayerNotFound() {

        String notExFiscalCode = "nonExistentPayerId";

        when(activationDBRepository.findByFiscalCode(notExFiscalCode))
            .thenReturn(Mono.empty());

        StepVerifier.create(activationPayerService.findPayer(notExFiscalCode))
            .verifyComplete();

        verify(activationDBRepository).findByFiscalCode(notExFiscalCode);
    }
}
