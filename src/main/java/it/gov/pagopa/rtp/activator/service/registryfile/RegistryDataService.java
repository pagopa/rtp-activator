package it.gov.pagopa.rtp.activator.service.registryfile;

import it.gov.pagopa.rtp.activator.domain.registryfile.ServiceProviderFullData;
import java.util.Map;
import reactor.core.publisher.Mono;

public interface RegistryDataService {

  Mono<Map<String, ServiceProviderFullData>> getRegistryData();

}
