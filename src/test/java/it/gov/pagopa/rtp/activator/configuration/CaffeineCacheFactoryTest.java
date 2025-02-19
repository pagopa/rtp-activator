package it.gov.pagopa.rtp.activator.configuration;

import static org.junit.jupiter.api.Assertions.*;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class CaffeineCacheFactoryTest {

  private final CaffeineCacheFactory caffeineCacheFactory = new CaffeineCacheFactory();

  @Test
  void givenValidParams_whenCreateCache_thenCacheHasCorrectMaxSize() {

    int maxSize = 100;
    Duration expireAfterWrite = Duration.ofMinutes(10);

    Caffeine<Object, Object> cache = caffeineCacheFactory.createCache(maxSize, expireAfterWrite);

    assertNotNull(cache);
    assertEquals(maxSize, cache.build().policy().eviction().orElseThrow().getMaximum());
  }

  @Test
  void givenValidParams_whenCreateCache_thenCacheHasCorrectExpiration() {

    int maxSize = 50;
    Duration expireAfterWrite = Duration.ofHours(1);

    Caffeine<Object, Object> cache = caffeineCacheFactory.createCache(maxSize, expireAfterWrite);

    assertNotNull(cache);
    assertEquals(expireAfterWrite, cache.build().policy().expireAfterWrite().orElseThrow().getExpiresAfter());
  }
}