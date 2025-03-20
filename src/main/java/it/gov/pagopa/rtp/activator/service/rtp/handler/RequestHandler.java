package it.gov.pagopa.rtp.activator.service.rtp.handler;

import reactor.core.publisher.Mono;

/**
 * A generic interface for handling requests of type {@code T}.
 * <p>
 * Implementations of this interface define how a specific type of request
 * should be processed asynchronously using {@link Mono}.
 *
 * @param <T> the type of request to be handled
 */
public interface RequestHandler<T> {

  /**
   * Handles the given request and returns a {@link Mono} representing
   * the asynchronous processing result.
   * <p>
   * The implementation should define how the request is processed and
   * what transformations or external interactions are performed.
   *
   * @param request the request to be handled
   * @return a {@link Mono} emitting the processed request or an error
   *         if processing fails
   */
  Mono<T> handle(T request);
}
