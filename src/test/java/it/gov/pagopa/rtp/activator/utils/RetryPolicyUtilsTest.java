package it.gov.pagopa.rtp.activator.utils;

import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.retry.RetryBackoffSpec;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RetryPolicyUtilsTest {

    @Test
    void givenValidRetryParams_whenSendRetryPolicy_thenReturnsRetryBackoffSpec() {
        ServiceProviderConfig.Send.Retry retryParams = mock(ServiceProviderConfig.Send.Retry.class);
        when(retryParams.maxAttempts()).thenReturn(3L);
        when(retryParams.backoffMinDuration()).thenReturn(1000L);
        when(retryParams.backoffJitter()).thenReturn(0.5d);

        RetryBackoffSpec retryPolicy = RetryPolicyUtils.sendRetryPolicy(retryParams);

        assertNotNull(retryPolicy);
        assertInstanceOf(RetryBackoffSpec.class, retryPolicy);
    }

    @Test
    void givenNullRetryParams_whenSendRetryPolicy_thenThrowsNullPointerException() {
        ServiceProviderConfig.Send.Retry retryParams = null;

        NullPointerException exception = assertThrows(NullPointerException.class, () ->
                RetryPolicyUtils.sendRetryPolicy(retryParams));
        assertEquals("Retry parameters cannot be null", exception.getMessage());
    }

    @Test
    void givenFailingMono_whenUsingRetryPolicy_thenRetriesAndSucceeds() {
        ServiceProviderConfig.Send.Retry retryParams = new ServiceProviderConfig.Send.Retry(3, 100, 0.0);

        var retrySpec = RetryPolicyUtils.sendRetryPolicy(retryParams);
        AtomicInteger counter = new AtomicInteger();

        Mono<String> unreliableMono = Mono.defer(() -> {
            if (counter.incrementAndGet() < 3) {
                return Mono.error(new RuntimeException("Temporary failure"));
            } else {
                return Mono.just("Success!");
            }
        }).retryWhen(retrySpec);

        StepVerifier.create(unreliableMono)
                .expectNext("Success!")
                .verifyComplete();

        assertEquals(3, counter.get());
    }

    @Test
    void givenAlwaysFailingMono_whenUsingRetryPolicy_thenRetriesAndFailsWithOriginalCause() {
    ServiceProviderConfig.Send.Retry retryParams =
        new ServiceProviderConfig.Send.Retry(2, 100, 0.0);
        var retrySpec = RetryPolicyUtils.sendRetryPolicy(retryParams);

        Mono<String> alwaysFailingMono = Mono.<String>error(new RuntimeException("Always fails"))
                .retryWhen(retrySpec);

        StepVerifier.create(alwaysFailingMono)
                .expectErrorMatches(throwable ->
                        throwable.getCause() instanceof RuntimeException &&
                                throwable.getCause().getMessage().contains("Always fails"))
                .verify();
    }



}
