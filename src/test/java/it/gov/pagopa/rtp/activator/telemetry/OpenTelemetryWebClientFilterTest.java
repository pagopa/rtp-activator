package it.gov.pagopa.rtp.activator.telemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.*;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import static org.mockito.Mockito.*;

public class OpenTelemetryWebClientFilterTest {

    private Tracer tracer;
    private Span span;
    private Scope scope;
    private OpenTelemetryWebClientFilter filter;

    @BeforeEach
    void setup() {
        tracer = mock(Tracer.class);
        span = mock(Span.class);
        scope = mock(Scope.class);
        var spanBuilder = mock(io.opentelemetry.api.trace.SpanBuilder.class);

        when(tracer.spanBuilder(anyString())).thenReturn(spanBuilder);
        when(spanBuilder.setSpanKind(SpanKind.CLIENT)).thenReturn(spanBuilder);
        when(spanBuilder.startSpan()).thenReturn(span);
        when(span.makeCurrent()).thenReturn(scope);

        filter = new OpenTelemetryWebClientFilter(tracer);
    }

    @Test
    void givenValidRequest_whenFilterApplied_thenSpanAndScopeAdded() {
        ClientRequest request = ClientRequest.create(org.springframework.http.HttpMethod.GET, URI.create("http://localhost/test")).build();
        ExchangeFunction exchangeFunction = r -> Mono.just(ClientResponse.create(org.springframework.http.HttpStatus.OK).build());

        ExchangeFilterFunction filterFunction = filter.filter();
        Mono<ClientResponse> result = filterFunction.filter(request, exchangeFunction);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode().is2xxSuccessful())
                .verifyComplete();

        verify(span).setStatus(StatusCode.OK);
        verify(span).setAttribute(eq("http.status_code"), eq(200L));
        verify(span).end();
        verify(scope).close();
    }

    @Test
    void givenErrorResponse_whenFilterApplied_thenSpanRecordsExceptionAndSetsErrorStatus() {
        RuntimeException error = new RuntimeException("Boom!");
        ClientRequest request = ClientRequest.create(org.springframework.http.HttpMethod.GET, URI.create("http://localhost/test")).build();
        ExchangeFunction exchangeFunction = r -> Mono.error(error);

        ExchangeFilterFunction filterFunction = filter.filter();
        Mono<ClientResponse> result = filterFunction.filter(request, exchangeFunction);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable.getMessage().equals("Boom!"))
                .verify();

        verify(span).recordException(error);
        verify(span).setStatus(StatusCode.ERROR);
        verify(span).end();
        verify(scope).close();
    }

    @Test
    void givenNoSpanOrScope_whenFilterApplied_thenWarnLoggedAndNoErrorsThrown() {
        ClientRequest request = ClientRequest.create(org.springframework.http.HttpMethod.GET, URI.create("http://localhost/test"))
                .build();

        ExchangeFunction exchangeFunction = r -> Mono.just(ClientResponse.create(org.springframework.http.HttpStatus.OK).build());

        ExchangeFilterFunction fullFilter = filter.filter();
        Mono<ClientResponse> result = fullFilter.filter(request, exchangeFunction);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode().is2xxSuccessful())
                .verifyComplete();
    }

    @Test
    void givenRequestWithoutScope_whenFilterApplied_thenSpanHandledButScopeSkipped() {
        ClientRequest request = ClientRequest.create(org.springframework.http.HttpMethod.GET, URI.create("http://localhost/test"))
                .attribute("otel-span", span)
                .build();

        ExchangeFunction exchangeFunction = r -> Mono.just(ClientResponse.create(org.springframework.http.HttpStatus.OK).build());

        ExchangeFilterFunction filterFunction = filter.filter();
        Mono<ClientResponse> result = filterFunction.filter(request, exchangeFunction);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode().is2xxSuccessful())
                .verifyComplete();

        verify(span).setStatus(StatusCode.OK);
        verify(span).setAttribute(eq("http.status_code"), eq(200L));
        verify(span).end();
    }

    @Test
    void givenRequestWithoutSpan_whenFilterApplied_thenScopeClosedButNoSpanHandling() {
        Span span = mock(Span.class);
        ClientRequest request = ClientRequest.create(org.springframework.http.HttpMethod.GET, URI.create("http://localhost/test"))
                .attribute("otel-scope", scope)
                .build();

        ExchangeFunction exchangeFunction = r -> Mono.just(ClientResponse.create(org.springframework.http.HttpStatus.OK).build());

        ExchangeFilterFunction filterFunction = filter.filter();
        Mono<ClientResponse> result = filterFunction.filter(request, exchangeFunction);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode().is2xxSuccessful())
                .verifyComplete();
        verify(scope).close();
        verifyNoInteractions(span);
    }
}

