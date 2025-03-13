package it.gov.pagopa.rtp.activator.service.rtp.handler;

import reactor.core.publisher.Mono;

public interface RequestHandler<T> {

  Mono<T> handle(T request);

}
