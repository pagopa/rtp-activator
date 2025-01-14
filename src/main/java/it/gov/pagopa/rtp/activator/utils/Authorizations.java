package it.gov.pagopa.rtp.activator.utils;

import java.util.function.BiPredicate;
import java.util.function.Function;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

public final class Authorizations {

  private Authorizations() {}

  /**
   * Verifies that the subject in the request matches the authenticated user's subject. It uses the
   * provided {@code extractSubject} function to extract the subject from the request object, and
   * compares it with the authenticated user's name.
   *
   * @param <T> The type of the request body.
   * @param requestBody A {@link Mono} containing the request body that needs to be verified.
   * @param extractSubject A function that extracts the subject (e.g., user identifier) from the
   *     request body.
   * @return A {@link Mono} containing the request body if the subjects match, or an error if they
   *     don't.
   */
  public static <T> Mono<T> verifySubjectRequest(
      Mono<T> requestBody, Function<T, String> extractSubject) {
    return verifyRequestBody(
        requestBody, (request, auth) -> extractSubject.apply(request).equals(auth.getName()));
  }

  /**
   * Verifies that the request body passes a custom verification function that involves the
   * authenticated user. This method takes a {@link Mono} of the request body and checks the
   * provided {@code verify} predicate to ensure the request meets the security requirements. If the
   * predicate fails, an {@link AccessDeniedException} is thrown.
   *
   * @param <T> The type of the request body.
   * @param requestBody A {@link Mono} containing the request body that needs to be verified.
   * @param verify A {@link BiPredicate} that performs a custom verification on the request body and
   *     the authenticated user.
   * @return A {@link Mono} containing the request body if the verification succeeds.
   */
  public static <T> Mono<T> verifyRequestBody(
      Mono<T> requestBody, BiPredicate<T, Authentication> verify) {
    return ReactiveSecurityContextHolder.getContext()
        .flatMap(
            securityContext ->
                requestBody.flatMap(
                    request ->
                        verify.test(request, securityContext.getAuthentication())
                            ? Mono.just(request)
                            : Mono.error(
                                new AccessDeniedException(
                                    "Authenticated user doesn't have permission to perform this"
                                        + " action."))));
  }
}
