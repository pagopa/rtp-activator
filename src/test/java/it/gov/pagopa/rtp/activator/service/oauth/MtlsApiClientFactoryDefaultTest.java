package it.gov.pagopa.rtp.activator.service.oauth;

import it.gov.pagopa.rtp.activator.configuration.mtlswebclient.MtlsWebClientFactory;
import it.gov.pagopa.rtp.activator.epcClient.invoker.ApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MtlsApiClientFactoryDefaultTest {

   @Mock
   private MtlsWebClientFactory mtlsWebClientFactory;
   
   @Mock
   private WebClient webClient;
   
   private MtlsApiClientFactory mtlsApiClientFactory;
   
   @BeforeEach
   void setUp() {
       mtlsApiClientFactory = new MtlsApiClientFactoryDefault(mtlsWebClientFactory);
   }
   
   @Test
   void createMtlsApiClient_CreatesApiClientWithCorrectBasePath() {
       String basePath = "https://test-api.example.com";
       when(mtlsWebClientFactory.createMtlsWebClient()).thenReturn(webClient);
       
       ApiClient result = mtlsApiClientFactory.createMtlsApiClient(basePath);
       
       assertNotNull(result);
       assertEquals(basePath, result.getBasePath());
       verify(mtlsWebClientFactory).createMtlsWebClient();
   }
}