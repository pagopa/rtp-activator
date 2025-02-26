package it.gov.pagopa.rtp.activator.configuration;

import javax.net.ssl.SSLContext;

public interface SslContextFactory {

  SSLContext getSslContext();

}
