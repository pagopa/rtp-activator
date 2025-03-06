package it.gov.pagopa.rtp.activator.service.oauth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import it.gov.pagopa.rtp.activator.configuration.mtlswebclient.MtlsWebClientFactory;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Oauth2TokenServiceImplTest {

    @Mock
    private MtlsWebClientFactory mtlsWebClientFactory;
    
    @Mock
    private WebClient webClient;
    
    @Mock
    private RequestBodyUriSpec requestBodyUriSpec;
    
    @Mock
    private RequestBodySpec requestBodySpec;
    
    @Mock
    private RequestHeadersSpec requestHeadersSpec;
    
    @Mock
    private ResponseSpec responseSpec;
    
    private Oauth2TokenService oauth2TokenService;
    
    @BeforeEach
    void setUp() {
        oauth2TokenService = new Oauth2TokenServiceImpl(mtlsWebClientFactory);
    }
    
    @Test
    void getAccessTokenWhenAllParamsProvidedReturnsToken() {
        String tokenUri = "https://example.com/token";
        String clientId = "test-client";
        String clientSecret = "test-secret";
        String scope = "test-scope";
        String accessToken = "fake-token-123";
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("access_token", accessToken);
        
        when(mtlsWebClientFactory.createMtlsWebClient()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(tokenUri)).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(responseMap));
        
        Mono<String> result = oauth2TokenService.getAccessToken(tokenUri, clientId, clientSecret, scope);
        
        StepVerifier.create(result)
            .expectNext(accessToken)
            .verifyComplete();
        
        verify(requestBodySpec).header(HttpHeaders.AUTHORIZATION, 
            "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8)));
    }
    
    @Test
    void getAccessTokenWhenScopeIsNullDoesNotAddScopeToForm() {
        String tokenUri = "https://example.com/token";
        String clientId = "test-client";
        String clientSecret = "test-secret";
        String accessToken = "fake-token-123";
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("access_token", accessToken);
        
        when(mtlsWebClientFactory.createMtlsWebClient()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(tokenUri)).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(responseMap));
        
        Mono<String> result = oauth2TokenService.getAccessToken(tokenUri, clientId, clientSecret, null);
        
        StepVerifier.create(result)
            .expectNext(accessToken)
            .verifyComplete();
    }
    
    @Test
    void getAccessTokenWhenClientIdIsNullReturnsError() {
        Mono<String> result = oauth2TokenService.getAccessToken("https://example.com/token", null, "secret", "scope");
        
        StepVerifier.create(result)
            .expectError(IllegalStateException.class)
            .verify();
    }
    
    @Test
    void getAccessTokenWhenClientSecretIsNullReturnsError() {
        Mono<String> result = oauth2TokenService.getAccessToken("https://example.com/token", "client", null, "scope");
        
        StepVerifier.create(result)
            .expectError(IllegalStateException.class)
            .verify();
    }
    
    @Test
    void getAccessTokenWhenTokenUriIsNullReturnsError() {
        Mono<String> result = oauth2TokenService.getAccessToken(null, "client", "secret", "scope");
        
        StepVerifier.create(result)
            .expectError(IllegalStateException.class)
            .verify();
    }
    
    @Test
    void getAccessTokenWhenRequestFailsPropagatesError() {
        String tokenUri = "https://example.com/token";
        String clientId = "test-client";
        String clientSecret = "test-secret";
        String scope = "test-scope";
        
        when(mtlsWebClientFactory.createMtlsWebClient()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(tokenUri)).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.error(new RuntimeException("Network error")));
        
        Mono<String> result = oauth2TokenService.getAccessToken(tokenUri, clientId, clientSecret, scope);
        
        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();
    }
    
    @Test
    void getAccessTokenWhenResponseMissingAccessTokenReturnsError() {
        String tokenUri = "https://example.com/token";
        String clientId = "test-client";
        String clientSecret = "test-secret";
        String scope = "test-scope";
        
        Map<String, Object> responseMap = new HashMap<>();
        
        when(mtlsWebClientFactory.createMtlsWebClient()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(tokenUri)).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(responseMap));
        
        Mono<String> result = oauth2TokenService.getAccessToken(tokenUri, clientId, clientSecret, scope);
        
        StepVerifier.create(result)
            .expectError(NullPointerException.class)
            .verify();
    }
}