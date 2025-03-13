package it.gov.pagopa.rtp.activator.configuration.mtlswebclient;

import org.springframework.web.reactive.function.client.WebClient;

public interface MtlsWebClientFactory {
    WebClient createSimpleWebClient();
    WebClient createMtlsWebClient();
}
