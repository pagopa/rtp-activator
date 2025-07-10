package it.gov.pagopa.rtp.activator.repository.activation;

import it.gov.pagopa.rtp.activator.repository.activation.ActivationEntity.Fields;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ActivationRepositoryExtendedImpl implements ActivationRepositoryExtended {

  private final ReactiveMongoTemplate mongoTemplate;

  public ActivationRepositoryExtendedImpl(ReactiveMongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public Flux<ActivationEntity> findByServiceProviderDebtor(String serviceProviderDebtor,
      Pageable pageable) {
    return mongoTemplate
        .find(
            Query.query(Criteria.where(Fields.serviceProviderDebtor).is(serviceProviderDebtor))
                .with(getPageable(pageable)),
            ActivationEntity.class
        );
  }

  @Override
  public Mono<Long> countByServiceProviderDebtor(String serviceProviderDebtor) {
    Query query = Query.query(Criteria.where("serviceProviderDebtor").is(serviceProviderDebtor));
    return mongoTemplate.count(query, ActivationEntity.class);
  }

  private Pageable getPageable(Pageable pageable) {
    return pageable != null ? pageable : Pageable.unpaged();
  }
}
