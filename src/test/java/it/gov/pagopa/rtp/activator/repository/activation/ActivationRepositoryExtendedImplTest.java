package it.gov.pagopa.rtp.activator.repository.activation;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ActivationRepositoryExtendedImplTest {

  private ReactiveMongoTemplate mongoTemplate;
  private ActivationRepositoryExtendedImpl repository;

  @BeforeEach
  void setUp() {
    mongoTemplate = mock(ReactiveMongoTemplate.class);
    repository = new ActivationRepositoryExtendedImpl(mongoTemplate);
  }

  @Test
  void testFindByServiceProviderDebtor_withPageable() {
    String serviceProviderDebtor = "serviceProviderDebtor";
    Pageable pageable = PageRequest.of(0, 5);
    ActivationEntity entity1 = new ActivationEntity();
    ActivationEntity entity2 = new ActivationEntity();

    when(mongoTemplate.find(any(Query.class), eq(ActivationEntity.class)))
        .thenReturn(Flux.just(entity1, entity2));

    StepVerifier.create(repository.findByServiceProviderDebtor(serviceProviderDebtor, pageable))
        .expectNext(entity1, entity2)
        .verifyComplete();

    ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
    verify(mongoTemplate).find(queryCaptor.capture(), eq(ActivationEntity.class));
    Query usedQuery = queryCaptor.getValue();
    assert usedQuery.getQueryObject().get("serviceProviderDebtor").equals(serviceProviderDebtor);
  }

  @Test
  void testFindByServiceProviderDebtor_withNullPageable() {
    String serviceProviderDebtor = "serviceProviderDebtor";
    ActivationEntity entity = new ActivationEntity();

    when(mongoTemplate.find(any(Query.class), eq(ActivationEntity.class)))
        .thenReturn(Flux.just(entity));

    StepVerifier.create(repository.findByServiceProviderDebtor(serviceProviderDebtor, null))
        .expectNext(entity)
        .verifyComplete();

    verify(mongoTemplate).find(any(Query.class), eq(ActivationEntity.class));
  }

  @Test
  void testCountByServiceProviderDebtor() {
    String serviceProviderDebtor = "serviceProviderDebtor";
    when(mongoTemplate.count(any(Query.class), eq(ActivationEntity.class)))
        .thenReturn(Mono.just(5L));

    StepVerifier.create(repository.countByServiceProviderDebtor(serviceProviderDebtor))
        .expectNext(5L)
        .verifyComplete();

    ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
    verify(mongoTemplate).count(queryCaptor.capture(), eq(ActivationEntity.class));
    Query usedQuery = queryCaptor.getValue();
    assert usedQuery.getQueryObject().get("serviceProviderDebtor").equals(serviceProviderDebtor);
  }
}

