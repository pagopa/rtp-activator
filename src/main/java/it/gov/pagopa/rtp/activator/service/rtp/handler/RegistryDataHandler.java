package it.gov.pagopa.rtp.activator.service.rtp.handler;

import it.gov.pagopa.rtp.activator.domain.errors.ServiceProviderNotFoundException;
import it.gov.pagopa.rtp.activator.service.registryfile.RegistryDataService;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component("registryDataHandler")
@Slf4j
public class RegistryDataHandler implements RequestHandler<EpcRequest> {

  private final RegistryDataService registryDataService;


  public RegistryDataHandler(@NonNull final RegistryDataService registryDataService) {
    this.registryDataService = Objects.requireNonNull(registryDataService);
  }


  @NonNull
  @Override
  public Mono<EpcRequest> handle(@NonNull final EpcRequest request) {
    return this.registryDataService.getRegistryData()
        .doFirst(() -> log.info("Calling registry data service. Request: {}", request))
        .doOnNext(data -> log.info("Successfully called registry data."))
        .flatMap(data ->
            Mono.justOrEmpty(data.get(request.rtpToSend().serviceProviderDebtor())))
        .doOnNext(data -> log.info("Successfully extracted service provider data."))
        .switchIfEmpty(Mono.error(new ServiceProviderNotFoundException(
            "No service provider found for creditor: " + request.rtpToSend().serviceProviderDebtor())))
        .map(request::withServiceProviderFullData)
        .doOnSuccess(data -> log.info("Successfully retrieved registry data for creditor: {}", data))
        .doOnError(error -> log.error("Error retrieving registry data: {}", error.getMessage(), error));
  }
}
