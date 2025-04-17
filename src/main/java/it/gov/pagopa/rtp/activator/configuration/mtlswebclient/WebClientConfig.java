package it.gov.pagopa.rtp.activator.configuration.mtlswebclient;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.spring.webflux.v5_3.SpringWebfluxClientTelemetry;
import java.util.Objects;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class WebClientConfig {

    @Bean("webClientBuilder")
    @NonNull
    public WebClient.Builder webClientBuilder(@NonNull final OpenTelemetry openTelemetry){
        Objects.requireNonNull(openTelemetry, "OpenTelemetry bean cannot be null.");

        final var springWebfluxClientTelemetry = SpringWebfluxClientTelemetry.builder(openTelemetry)
            .build();

        return WebClient.builder()
            .filters(springWebfluxClientTelemetry::addFilter);
    }
}
