package it.gov.pagopa.rtp.activator.configuration;

import org.springframework.web.reactive.function.client.WebClient;

/**
 * A factory interface for creating instances of OpenAPI clients.
 *
 * @param <T> the type of the OpenAPI client that this factory creates
 */
public interface OpenAPIClientFactory<T> {

  /**
   * Creates an instance of the OpenAPI client using the provided WebClient.
   *
   * @param webClient the WebClient to be used for making API calls
   * @return an instance of the OpenAPI client
   */
  T createClient(WebClient webClient);
}