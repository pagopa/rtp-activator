package it.gov.pagopa.rtp.activator.utils;

import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.util.Objects;

/**
 * Utility class for creating retry policies using Project Reactor's {@link Retry} API.
 *
 * <p>This class provides helper methods to configure retry strategies, such as backoff duration,
 * jitter, and maximum attempts, based on application configuration.
 */
@Slf4j
public class RetryPolicyUtils {

    /**
     * Creates a {@link RetryBackoffSpec} based on the provided retry configuration parameters.
     *
     * <p>This retry policy uses exponential backoff with optional jitter to space out retry attempts.
     * Each retry is logged with its current attempt count.
     *
     * @param retryParams the configuration parameters for retry (must not be null)
     * @return a configured {@link RetryBackoffSpec} instance
     * @throws NullPointerException if {@code retryParams} is null
     */
    @NonNull
    public static RetryBackoffSpec sendRetryPolicy(@NonNull final ServiceProviderConfig.Send.Retry retryParams) {

        Objects.requireNonNull(retryParams, "Retry parameters cannot be null");

        final var maxAttempts = retryParams.maxAttempts();
        final var minDurationMillis = retryParams.backoffMinDuration();
        final var jitter = retryParams.backoffJitter();

        return Retry.backoff(maxAttempts, Duration.ofMillis(minDurationMillis))
                .jitter(jitter)
                .doAfterRetry(signal -> log.info("Retry number {}", signal.totalRetries()));
    }
}
