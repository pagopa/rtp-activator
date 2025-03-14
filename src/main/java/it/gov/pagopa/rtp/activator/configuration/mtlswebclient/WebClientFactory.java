package it.gov.pagopa.rtp.activator.configuration.mtlswebclient;

import org.springframework.web.reactive.function.client.WebClient;

/**
 * Factory interface for creating instances of {@link WebClient}.
 * <p>
 * This interface provides methods to create both a standard {@link WebClient}
 * and a mutual TLS (mTLS) secured {@link WebClient}, depending on the
 * security requirements of the request.
 */
public interface WebClientFactory {

    /**
     * Creates a simple {@link WebClient} instance without mutual TLS (mTLS).
     * <p>
     * This method is typically used for scenarios where secure communication
     * does not require client certificate authentication.
     *
     * @return a non-mTLS configured {@link WebClient} instance
     */
    WebClient createSimpleWebClient();

    /**
     * Creates a mutual TLS (mTLS) enabled {@link WebClient} instance.
     * <p>
     * This method should be used when communication requires client-side
     * certificate authentication to establish a secure connection.
     *
     * @return an mTLS-configured {@link WebClient} instance
     */
    WebClient createMtlsWebClient();
}

