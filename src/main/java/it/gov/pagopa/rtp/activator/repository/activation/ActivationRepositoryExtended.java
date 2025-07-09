package it.gov.pagopa.rtp.activator.repository.activation;

import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ActivationRepositoryExtended {

  Flux<ActivationEntity> findByServiceProviderDebtor(String serviceProviderDebtor, Pageable pageable);
  Mono<Long> countByServiceProviderDebtor(String serviceProviderDebtor);

}
