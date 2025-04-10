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
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@Slf4j
public class OpenTelemetryWebClientFilter {

    private static final String SPAN = "otel-span";
    private static final String SCOPE = "otel-scope";

    private final Tracer tracer;

    public OpenTelemetryWebClientFilter(Tracer tracer) {
        this.tracer = tracer;
    }

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
