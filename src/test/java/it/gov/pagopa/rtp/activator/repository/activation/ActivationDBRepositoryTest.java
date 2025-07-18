package it.gov.pagopa.rtp.activator.repository.activation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.gov.pagopa.rtp.activator.domain.payer.ActivationID;
import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ActivationDBRepositoryTest {

  @Mock
  private ActivationDB activationDB;

  @Mock
  private DeletedActivationDB deletedActivationDB;

  @Mock
  private ActivationMapper activationMapper;

  @Mock
  private ActivationRepositoryExtended activationRepositoryExtended;

  @InjectMocks
  private ActivationDBRepository repository;

  @Test
  void givenValidId_whenFindById_thenReturnMappedPayer() {
    final var id = UUID.randomUUID();
    final var entity = new ActivationEntity();
    final var expectedPayer = mock(Payer.class);

    when(activationDB.findById(id))
        .thenReturn(Mono.just(entity));
    when(activationMapper.toDomain(entity))
        .thenReturn(expectedPayer);

    StepVerifier.create(repository.findById(id))
        .expectNext(expectedPayer)
        .verifyComplete();

    verify(activationDB).findById(id);
    verify(activationMapper).toDomain(entity);
  }

  @Test
  void givenNullId_whenFindById_thenThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> repository.findById(null));
  }

  @Test
  void givenValidFiscalCode_whenFindByFiscalCode_thenReturnMappedPayer() {
    final var fiscalCode = "MOCKXX01D01H501W";
    final var entity = new ActivationEntity();
    final var expectedPayer = mock(Payer.class);

    when(activationDB.findByFiscalCode(fiscalCode))
        .thenReturn(Mono.just(entity));
    when(activationMapper.toDomain(entity))
        .thenReturn(expectedPayer);

    StepVerifier.create(repository.findByFiscalCode(fiscalCode))
        .expectNext(expectedPayer)
        .verifyComplete();

    verify(activationDB).findByFiscalCode(fiscalCode);
    verify(activationMapper).toDomain(entity);
  }

  @Test
  void givenValidPayer_whenSave_thenMapAndReturnSavedPayer() {
    final var payer = mock(Payer.class);
    final var entity = new ActivationEntity();

    when(activationMapper.toDbEntity(payer))
        .thenReturn(entity);
    when(activationDB.save(entity))
        .thenReturn(Mono.just(entity));
    when(activationMapper.toDomain(entity))
        .thenReturn(payer);

    StepVerifier.create(repository.save(payer))
        .expectNext(payer)
        .verifyComplete();

    verify(activationMapper).toDbEntity(payer);
    verify(activationDB).save(entity);
    verify(activationMapper).toDomain(entity);
  }

  @Test
  void givenValidPayerAndReason_whenDeactivate_thenSaveToDeletedDbAndDeleteFromActivationDb() {
    final var payer = mock(Payer.class);
    final var activationId = UUID.randomUUID();

    final var deletedEntity = new DeletedActivationEntity();
    deletedEntity.setId(activationId);

    when(payer.activationID())
        .thenReturn(new ActivationID(activationId));
    when(activationMapper.toDeletedDbEntity(payer))
        .thenReturn(deletedEntity);
    when(deletedActivationDB.save(deletedEntity))
        .thenReturn(Mono.just(deletedEntity));
    when(activationDB.deleteById(activationId))
        .thenReturn(Mono.empty());

    StepVerifier.create(repository.deactivate(payer))
        .verifyComplete();

    verify(activationMapper).toDeletedDbEntity(payer);
    verify(deletedActivationDB).save(deletedEntity);
    verify(activationDB).deleteById(activationId);
  }

  @Test
  void givenNullPayer_whenDeactivate_thenThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> repository.deactivate(null));
  }

  @Test
  void givenValidPayer_whenSaveToDeletedDbFails_thenErrorIsPropagated() {
    final var payer = mock(Payer.class);
    final var activationId = UUID.randomUUID();

    final var deletedEntity = new DeletedActivationEntity();
    deletedEntity.setId(activationId);

    when(payer.activationID())
            .thenReturn(new ActivationID(activationId));
    when(activationMapper.toDeletedDbEntity(payer))
        .thenReturn(deletedEntity);
    when(deletedActivationDB.save(deletedEntity))
        .thenReturn(Mono.error(new IllegalArgumentException("DB save error")));

    StepVerifier.create(repository.deactivate(payer))
        .expectErrorMessage("DB save error")
        .verify();

    verify(deletedActivationDB).save(deletedEntity);
    verify(activationDB, never()).deleteById(any(UUID.class));
  }

  @Test
  void givenValidPayer_whenDeleteFromActivationDbFails_thenErrorIsPropagated() {
    final var payer = mock(Payer.class);
    final var activationId = UUID.randomUUID();

    final var deletedEntity = new DeletedActivationEntity();
    deletedEntity.setId(activationId);

    when(payer.activationID())
        .thenReturn(new ActivationID(activationId));
    when(activationMapper.toDeletedDbEntity(payer))
        .thenReturn(deletedEntity);
    when(deletedActivationDB.save(deletedEntity))
        .thenReturn(Mono.just(deletedEntity));
    when(activationDB.deleteById(activationId))
        .thenReturn(Mono.error(new IllegalArgumentException("Delete error")));

    StepVerifier.create(repository.deactivate(payer))
        .expectErrorMessage("Delete error")
        .verify();

    verify(deletedActivationDB).save(deletedEntity);
    verify(activationDB).deleteById(activationId);
  }

  @Test
  void whenGetActivationsByServiceProvider_thenReturnTuple2OfActivationEntityAndLong() {
    String serviceProvider = "serviceProvider";
    String fiscalCode = "fiscalCode";
    int page = 0;
    int size = 10;

    Payer payer = new Payer(new ActivationID(UUID.randomUUID()), serviceProvider, fiscalCode, Instant.now());

    ActivationEntity activationEntity = new ActivationEntity();
    List<ActivationEntity> activationEntityList = List.of(activationEntity, activationEntity);
    List<Payer> payerList = List.of(payer, payer);
    long expectedCount = 2L;

    when(activationRepositoryExtended.findByServiceProviderDebtor(serviceProvider, PageRequest.of(page, size)))
        .thenReturn(Flux.fromIterable(activationEntityList));
    when(activationMapper.toDomain(activationEntity)).thenReturn(payer);
    when(activationRepositoryExtended.countByServiceProviderDebtor(serviceProvider))
        .thenReturn(Mono.just(expectedCount));

    StepVerifier.create(repository.getActivationsByServiceProvider(serviceProvider, page, size))
        .assertNext(result -> {
          assertThat(result.getT1()).containsExactlyElementsOf(payerList);
          assertThat(result.getT2()).isEqualTo(expectedCount);
        })
        .verifyComplete();

    verify(activationRepositoryExtended).findByServiceProviderDebtor(serviceProvider, PageRequest.of(page, size));
    verify(activationRepositoryExtended).countByServiceProviderDebtor(serviceProvider);
  }
}
