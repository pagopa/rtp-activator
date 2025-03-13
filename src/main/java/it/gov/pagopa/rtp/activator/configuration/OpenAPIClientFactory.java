package it.gov.pagopa.rtp.activator.configuration;

import org.springframework.web.reactive.function.client.WebClient;

public interface OpenAPIClientFactory<T> {

  T createClient(WebClient webClient);

}
