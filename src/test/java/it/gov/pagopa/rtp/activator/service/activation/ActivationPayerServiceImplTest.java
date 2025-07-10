package it.gov.pagopa.rtp.activator.service.activation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.gov.pagopa.rtp.activator.domain.errors.PayerAlreadyExists;
import it.gov.pagopa.rtp.activator.domain.errors.PayerNotFoundException;
import it.gov.pagopa.rtp.activator.domain.payer.ActivationID;
import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.repository.activation.ActivationDBRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

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
  void givenUnknownId_whenFindPayerById_thenThrowsPayerNotFoundException() {

    final UUID unknownId = UUID.randomUUID();

    when(activationDBRepository.findById(unknownId)).thenReturn(Mono.empty());

    StepVerifier.create(activationPayerService.findPayerById(unknownId))
            .expectErrorMatches(throwable ->
                    throwable instanceof PayerNotFoundException &&
                            throwable.getMessage().contains(unknownId.toString())
            )
            .verify();

    verify(activationDBRepository).findById(unknownId);
  }

  @Test
  void givenGenericError_whenFindPayerById_thenErrorIsPropagated() {

    final UUID id = UUID.randomUUID();
    final RuntimeException genericError = new RuntimeException("Generic DB failure");

    when(activationDBRepository.findById(id)).thenReturn(Mono.error(genericError));

    StepVerifier.create(activationPayerService.findPayerById(id))
            .expectErrorMatches(throwable ->
                    throwable instanceof RuntimeException &&
                            throwable.getMessage().equals("Generic DB failure")
            )
            .verify();

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
    final var payerToDeactivate = new Payer(new ActivationID(activationId), rtpSpId, fiscalCode, Instant.now());

    when(activationDBRepository.deactivate(payerToDeactivate))
        .thenReturn(Mono.empty());

    StepVerifier.create(activationPayerService.deactivatePayer(payerToDeactivate))
        .expectNext(payerToDeactivate)
        .verifyComplete();

    verify(activationDBRepository).deactivate(payerToDeactivate);
  }

  @Test
  void givenNullPayer_whenDeactivatePayer_thenThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> activationPayerService.deactivatePayer(null));
    verify(activationDBRepository, never()).deactivate(any());
  }

  @Test
  void givenDeactivateFails_whenDeactivatePayer_thenErrorIsPropagated() {

    final var activationId = UUID.randomUUID();
    final var payerToDeactivate = new Payer(new ActivationID(activationId), rtpSpId, fiscalCode, Instant.now());

    when(activationDBRepository.deactivate(payerToDeactivate))
        .thenReturn(Mono.error(new RuntimeException("deactivation error")));

    StepVerifier.create(activationPayerService.deactivatePayer(payerToDeactivate))
        .expectErrorMessage("deactivation error")
        .verify();

    verify(activationDBRepository).deactivate(payerToDeactivate);
  }

  @Test
  void whenGetActivationsByServiceProvider_thenReturnsActivations() {
    List<Payer> activations = List.of(payer, payer);
    long totalCount = 2L;

    when(activationDBRepository.getActivationsByServiceProvider(rtpSpId, 0, 10))
        .thenReturn(Mono.just(Tuples.of(activations, totalCount)));

    StepVerifier.create(activationPayerService.getActivationsByServiceProvider(rtpSpId, 0, 10))
        .expectNextMatches(result ->
            result.getT1().size() == 2 &&
                result.getT2() == 2L
        )
        .verifyComplete();

    verify(activationDBRepository).getActivationsByServiceProvider(rtpSpId, 0, 10);
  }

  @Test
  void whenGetActivationsByServiceProviderFails_thenReturnsError() {
    DataAccessResourceFailureException exception =
        new DataAccessResourceFailureException("Database error");

    when(activationDBRepository.getActivationsByServiceProvider(rtpSpId, 0, 10))
        .thenReturn(Mono.error(exception));

    StepVerifier.create(activationPayerService.getActivationsByServiceProvider(rtpSpId, 0, 10))
        .expectErrorMatches(e -> e instanceof DataAccessResourceFailureException &&
            e.getMessage().equals("Database error"))
        .verify();

    verify(activationDBRepository).getActivationsByServiceProvider(rtpSpId, 0, 10);
  }
}
