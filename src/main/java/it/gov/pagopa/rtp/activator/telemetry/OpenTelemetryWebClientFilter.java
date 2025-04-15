package it.gov.pagopa.rtp.activator.telemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * WebClient filter for integrating OpenTelemetry tracing into HTTP client calls.
 * <p>
 * This filter creates a new OpenTelemetry {@link Span}
 * for each outgoing HTTP request initiated through Spring's {@link WebClient}.
 * It ensures the span is started before the request is executed, and properly
 * closed after the response is received or an error occurs.
 * <p>
 * The span is also set as the current context using {@link Scope}
 * to enable automatic context propagation.
 * <p>
 * The resulting spans can be exported to an OpenTelemetry-compatible backend
 * to enable distributed tracing and diagnostics.
 */
@Component
@Slf4j
public class OpenTelemetryWebClientFilter {

    private static final String SPAN = "otel-span";
    private static final String SCOPE = "otel-scope";

    private final Tracer tracer;

    /**
     * Constructs an {@code OpenTelemetryWebClientFilter} that instruments WebClient
     * calls with OpenTelemetry. The provided {@link Tracer} is used to create spans
     * for outbound HTTP requests.
     *
     * @param tracer the OpenTelemetry {@link Tracer} used to create spans
     */
    public OpenTelemetryWebClientFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Returns a {@link ExchangeFilterFunction} that instruments WebClient requests
     * with OpenTelemetry spans.
     * <p>
     * For each outgoing request, the filter:
     * <ul>
     *   <li>Creates a new span with name {@code API call: METHOD /path}</li>
     *   <li>Sets the span as the current context</li>
     *   <li>Attaches the span and context scope to the request attributes</li>
     * </ul>
     * After the request is processed:
     * <ul>
     *   <li>Marks the span as successful or records an error</li>
     *   <li>Ends the span and closes the scope</li>
     * </ul>
     *
     * @return an {@link ExchangeFilterFunction} for tracing WebClient calls
     */
    public ExchangeFilterFunction filter() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            final var spanName = "API call: " + request.method() + " " + request.url().getPath();
            final var span = tracer.spanBuilder(spanName)
                    .setSpanKind(SpanKind.CLIENT)
                    .startSpan();
            final var scope = span.makeCurrent();

            return Mono.just(ClientRequest.from(request)
                    .attribute(SPAN, span)
                    .attribute(SCOPE, scope)
                    .build());
        }).andThen((request, next) -> {

            final var span = Optional.of(request)
                    .map(ClientRequest::attributes)
                    .map(attrs -> attrs.get(SPAN))
                    .map(Span.class::cast);

            if (span.isEmpty()) {
                log.warn("No attributes found for span: {}", SPAN);
            }

            final var scope = Optional.of(request)
                    .map(ClientRequest::attributes)
                    .map(attrs -> attrs.get(SCOPE))
                    .map(Scope.class::cast);

            if (scope.isEmpty()) {
                log.warn("No attributes found for scope: {}", SCOPE);
            }

            return next.exchange(request)
                    .doOnSuccess(response -> span.ifPresent(s -> {
                        s.setStatus(StatusCode.OK);
                        s.setAttribute("http.status_code", response.statusCode()
                                .value());
                    }))
                    .doOnError(error -> span.ifPresent(s -> {
                        s.recordException(error);
                        s.setStatus(StatusCode.ERROR);
                    }))
                    .doFinally(signal -> {
                        span.ifPresent(Span::end);
                        scope.ifPresent(Scope::close);
                    });
        });
    }
}
