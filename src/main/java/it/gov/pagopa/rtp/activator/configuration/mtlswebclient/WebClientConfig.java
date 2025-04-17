package it.gov.pagopa.rtp.activator.configuration.mtlswebclient;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.spring.webflux.v5_3.SpringWebfluxClientTelemetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @Primary
    public WebClient.Builder webClientBuilder(@NonNull final OpenTelemetry openTelemetry){
        final var springWebfluxClientTelemetry = SpringWebfluxClientTelemetry.builder(openTelemetry)
            .build();

        final var webclient = WebClient.create();
        return webclient.mutate().filters(springWebfluxClientTelemetry::addFilter);
    }
}
