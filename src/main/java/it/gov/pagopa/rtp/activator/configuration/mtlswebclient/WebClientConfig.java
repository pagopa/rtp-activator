package it.gov.pagopa.rtp.activator.configuration.mtlswebclient;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.spring.webflux.v5_3.SpringWebfluxClientTelemetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private final SpringWebfluxClientTelemetry springWebfluxClientTelemetry;

    public WebClientConfig(OpenTelemetry openTelemetry) {
        this.springWebfluxClientTelemetry = SpringWebfluxClientTelemetry.builder(openTelemetry).build();
    }

    @Bean
    @Primary
    public WebClient.Builder webClientBuilder(){
        WebClient webclient = WebClient.create();
        return webclient.mutate().filters(springWebfluxClientTelemetry::addFilter);
    }
}
