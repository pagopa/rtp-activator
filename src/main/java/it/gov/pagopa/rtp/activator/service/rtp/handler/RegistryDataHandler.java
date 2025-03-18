package it.gov.pagopa.rtp.activator.service.rtp.handler;

import it.gov.pagopa.rtp.activator.domain.errors.ServiceProviderNotFoundException;
import it.gov.pagopa.rtp.activator.service.registryfile.RegistryDataService;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


/**
 * Handles registry data retrieval for an incoming EPC request.
 * This class interacts with the {@link RegistryDataService} to fetch service provider data
 * and update the request with relevant information.
 */
@Component("registryDataHandler")
@Slf4j
public class RegistryDataHandler implements RequestHandler<EpcRequest> {

  private final RegistryDataService registryDataService;

  /**
   * Constructs a {@code RegistryDataHandler} with the given registry data service.
   *
   * @param registryDataService the service used to retrieve registry data
   * @throws NullPointerException if {@code registryDataService} is null
   */
  public RegistryDataHandler(@NonNull final RegistryDataService registryDataService) {
    this.registryDataService = Objects.requireNonNull(registryDataService);
  }

  /**
   * Processes the given EPC request by fetching registry data and updating the request
   * with the retrieved service provider information.
   *
   * @param request the EPC request to be processed
   * @return a {@link Mono} emitting the updated {@link EpcRequest} or an error if
   *         the service provider is not found
   * @throws ServiceProviderNotFoundException if no matching service provider is found in the registry
   */
  @NonNull
  @Override
  public Mono<EpcRequest> handle(@NonNull final EpcRequest request) {
    return this.registryDataService.getRegistryData()
        .doFirst(() -> log.info("Calling registry data service"))
        .doOnNext(data -> log.info("Successfully called registry data."))
        .flatMap(data -> Mono.justOrEmpty(data.get(request.rtpToSend().serviceProviderDebtor())))
        .doOnNext(data -> log.info("Successfully extracted service provider data."))
        .switchIfEmpty(Mono.error(new ServiceProviderNotFoundException(
            "No service provider found for creditor: " + request.rtpToSend().serviceProviderDebtor())))
        .map(request::withServiceProviderFullData)
        .doOnSuccess(data -> log.info("Successfully retrieved registry data for creditor"))
        .doOnError(error -> log.error("Error retrieving registry data: {}", error.getMessage(), error));
  }
}

