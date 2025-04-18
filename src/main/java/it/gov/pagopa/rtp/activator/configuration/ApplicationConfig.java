package it.gov.pagopa.rtp.activator.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.spring.webflux.v5_3.SpringWebfluxClientTelemetry;
import it.gov.pagopa.rtp.activator.activateClient.api.ReadApi;
import it.gov.pagopa.rtp.activator.activateClient.invoker.ApiClient;
import it.gov.pagopa.rtp.activator.configuration.mtlswebclient.WebClientFactory;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApplicationConfig {

  @Bean("objectMapper")
  public ObjectMapper objectMapper() {
    return JsonMapper.builder()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .addModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build();
  }


  @Bean("webClient")
  @Primary
  public WebClient webClient(@NonNull final WebClientFactory webClientFactory) {
    return Optional.of(webClientFactory)
        .map(WebClientFactory::createSimpleWebClient)
        .orElseThrow(() -> new IllegalStateException("Couldn't create web client"));
  }


  @Bean("activationApiClient")
  public ApiClient apiClient(@Qualifier("webClient") WebClient webClient) {
    return new ApiClient(webClient);
  }


  @Bean("activationApi")
  public ReadApi readApi(ApiClient apiClient) {
    return new ReadApi(apiClient);
  }

}
