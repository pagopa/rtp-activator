package it.gov.pagopa.rtp.activator.configuration.MtlsWebClient;

import org.springframework.web.reactive.function.client.WebClient;

public interface MtlsWebClientFactory {
    public WebClient createMtlsWebClient();
}
