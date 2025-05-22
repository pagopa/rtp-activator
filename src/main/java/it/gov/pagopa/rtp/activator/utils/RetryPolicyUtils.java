package it.gov.pagopa.rtp.activator.utils;

import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.util.Objects;

@Slf4j
public class RetryPolicyUtils {

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
