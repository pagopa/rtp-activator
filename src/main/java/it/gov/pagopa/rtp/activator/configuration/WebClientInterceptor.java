package it.gov.pagopa.rtp.activator.configuration;

import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServerBearerExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

public class WebClientInterceptor {

  private WebClientInterceptor() {}

  public static WebClient createWebClient(WebClient.Builder builder) {
    return builder
        // Bearer token propagation from the context security
        .filter(new ServerBearerExchangeFilterFunction())
        .build();
  }
}
