package it.gov.pagopa.rtp.activator.service.activation;

import it.gov.pagopa.rtp.activator.domain.payer.DeactivationReason;
import java.util.UUID;
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
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivationPayerServiceImplTest {

  @Mock private ActivationDBRepository activationDBRepository;

  @InjectMocks private ActivationPayerServiceImpl activationPayerService;

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

    when(activationDBRepository.save(any(Payer.class)))
        .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

    StepVerifier.create(activationPayerService.activatePayer(rtpSpId, fiscalCode))
        .expectNextMatches(pay -> {
              // Verify payer details
              assert pay.serviceProviderDebtor().equals(rtpSpId);
              assert pay.fiscalCode().equals(fiscalCode);
              assert pay.activationID() != null;
              assert pay.effectiveActivationDate() != null;
              return true;
        })
        .verifyComplete();

    verify(activationDBRepository).save(any(Payer.class));
  }

  @Test
  void testActivatePayerAlreadyExists() {

    when(activationDBRepository.save(any(Payer.class)))
        .thenReturn(Mono.error(new DuplicateKeyException("duplicate")));

    StepVerifier.create(activationPayerService.activatePayer(rtpSpId, fiscalCode))
        .expectError(PayerAlreadyExists.class)
        .verify();
  }

  @Test
  void testFindPayerSuccessful() {
    when(activationDBRepository.findByFiscalCode(fiscalCode)).thenReturn(Mono.just(payer));

    StepVerifier.create(activationPayerService.findPayer(fiscalCode))
        .expectNextMatches(pay -> pay.equals(payer))
        .verifyComplete();

    verify(activationDBRepository).findByFiscalCode(fiscalCode);
  }

  @Test
  void testFindPayerNotFound() {

    String notExFiscalCode = "nonExistentPayerId";

    when(activationDBRepository.findByFiscalCode(notExFiscalCode)).thenReturn(Mono.empty());

    StepVerifier.create(activationPayerService.findPayer(notExFiscalCode)).verifyComplete();

    verify(activationDBRepository).findByFiscalCode(notExFiscalCode);
  }

  @Test
  void givenValidId_whenFindPayerById_thenReturnsPayer() {

    final var id = UUID.randomUUID();
    final var expectedPayer = new Payer(new ActivationID(id), rtpSpId, fiscalCode, Instant.now());

    when(activationDBRepository.findById(id)).thenReturn(Mono.just(expectedPayer));

    StepVerifier.create(activationPayerService.findPayerById(id))
        .expectNext(expectedPayer)
        .verifyComplete();

    verify(activationDBRepository).findById(id);
  }

  @Test
  void givenNullId_whenFindPayerById_thenThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> activationPayerService.findPayerById(null));
    verify(activationDBRepository, never()).findById(any());
  }

  @Test
  void givenValidPayer_whenDeactivatePayer_thenCompletesSuccessfully() {

    final var activationId = UUID.randomUUID();
    final var payer = new Payer(new ActivationID(activationId), rtpSpId, fiscalCode, Instant.now());

    when(activationDBRepository.deactivate(payer, DeactivationReason.DELETE))
        .thenReturn(Mono.empty());

    StepVerifier.create(activationPayerService.deactivatePayer(payer))
        .verifyComplete();

    verify(activationDBRepository).deactivate(payer, DeactivationReason.DELETE);
  }

  @Test
  void givenNullPayer_whenDeactivatePayer_thenThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> activationPayerService.deactivatePayer(null));
    verify(activationDBRepository, never()).deactivate(any(), any());
  }

  @Test
  void givenDeactivateFails_whenDeactivatePayer_thenErrorIsPropagated() {

    final var activationId = UUID.randomUUID();
    final var payer = new Payer(new ActivationID(activationId), rtpSpId, fiscalCode, Instant.now());

    when(activationDBRepository.deactivate(payer, DeactivationReason.DELETE))
        .thenReturn(Mono.error(new RuntimeException("deactivation error")));

    StepVerifier.create(activationPayerService.deactivatePayer(payer))
        .expectErrorMessage("deactivation error")
        .verify();

    verify(activationDBRepository).deactivate(payer, DeactivationReason.DELETE);
  }
}
