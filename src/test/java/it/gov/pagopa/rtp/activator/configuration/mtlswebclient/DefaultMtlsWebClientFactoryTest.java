package it.gov.pagopa.rtp.activator.configuration.mtlswebclient;

import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import it.gov.pagopa.rtp.activator.configuration.ssl.SslContextFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContext;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultMtlsWebClientFactoryTest {

   @Mock
   private SslContextFactory sslContextFactory;
   @Mock
   private ServiceProviderConfig config;

   @Mock
   private SslContext sslContext;

   private DefaultMtlsWebClientFactory mtlsWebClientFactory;

   @BeforeEach
   void setUp() {
       mtlsWebClientFactory = new DefaultMtlsWebClientFactory(sslContextFactory, config);
   }

   @Test
   void createMtlsWebClient_CreatesWebClientWithSslContext() {
       when(sslContextFactory.getSslContext()).thenReturn(sslContext);
       when(config.send().timeout()).thenReturn(10000L);

       WebClient result = mtlsWebClientFactory.createMtlsWebClient();

       assertNotNull(result);
       verify(sslContextFactory).getSslContext();
   }
}