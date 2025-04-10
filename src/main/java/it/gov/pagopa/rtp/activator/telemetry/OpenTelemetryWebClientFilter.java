package it.gov.pagopa.rtp.activator.telemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

public class OpenTelemetryWebClientFilter {

    public static ExchangeFilterFunction create(Tracer tracer) {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            String spanName = "Chiamata a API Pagopa: " + request.method() + " " + request.url().getPath();
            Span span = tracer.spanBuilder(spanName)
                    .setSpanKind(SpanKind.CLIENT)
                    .startSpan();
            Scope scope = span.makeCurrent();

            return Mono.just(ClientRequest.from(request)
                    .attribute("otel-span", span)
                    .attribute("otel-scope", scope)
                    .build());
        }).andThen((request, next) -> {
            Span span = (Span) request.attributes().get("otel-span");
            Scope scope = (Scope) request.attributes().get("otel-scope");

            return next.exchange(request)
                    .doOnSuccess(response -> {
                        if (span != null) {
                            span.setStatus(StatusCode.OK);
                        }
                    })
                    .doOnError(error -> {
                        if (span != null) {
                            span.recordException(error);
                            span.setStatus(StatusCode.ERROR);
                        }
                    })
                    .doFinally(signal -> {
                        if (scope != null) {
                            scope.close();
                        }
                        if (span != null) {
                            span.end();
                        }
                    });
        });
    }
}
